package com.creator.settlement.settlement.service;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.common.time.KstPeriodResolver;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.repository.SaleCancellationRepository;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import com.creator.settlement.settlement.domain.Settlement;
import com.creator.settlement.settlement.dto.RegisterSettlementCommand;
import com.creator.settlement.settlement.dto.SettlementResult;
import com.creator.settlement.settlement.repository.SettlementRepository;
import com.creator.settlement.settlement.support.SettlementCalculator;
import com.creator.settlement.settlement.support.SettlementPolicy;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class SettlementCommandService {

    private final CreatorRepository creatorRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final SaleCancellationRepository saleCancellationRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementCalculator settlementCalculator;
    private final KstPeriodResolver kstPeriodResolver;
    private final KstClock kstClock;

    public SettlementResult registerSettlement(RegisterSettlementCommand command) {
        Creator creator = creatorRepository.findById(command.creatorId())
                .orElseThrow(() -> new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + command.creatorId()));

        validateCreatableSettlementMonth(command.settlementMonth());

        if (settlementRepository.existsByCreator_IdAndSettlementMonth(creator.getId(), command.settlementMonth())) {
            throw new BusinessRuleViolationException("해당 크리에이터의 정산이 이미 존재합니다: "
                    + creator.getId() + " / " + command.settlementMonth());
        }

        String settlementId = resolveSettlementId(command.settlementId());
        if (settlementRepository.existsById(settlementId)) {
            throw new BusinessRuleViolationException("이미 존재하는 정산 ID입니다: " + settlementId);
        }

        KstPeriodResolver.KstRange monthlyRange = kstPeriodResolver.monthlyRange(command.settlementMonth());
        List<SaleRecord> saleRecords = saleRecordRepository
                .findAllByCourseCreatorIdAndPaidAtGreaterThanEqualAndPaidAtLessThan(
                        creator.getId(),
                        monthlyRange.startAt(),
                        monthlyRange.endExclusive()
                );
        List<SaleCancellation> saleCancellations = saleCancellationRepository
                .findAllBySaleRecordCourseCreatorIdAndCanceledAtGreaterThanEqualAndCanceledAtLessThan(
                        creator.getId(),
                        monthlyRange.startAt(),
                        monthlyRange.endExclusive()
                );

        SettlementCalculator.SettlementAmounts amounts =
                settlementCalculator.calculate(saleRecords, saleCancellations);

        Settlement settlement = settlementRepository.save(Settlement.builder()
                .id(settlementId)
                .creator(creator)
                .settlementMonth(command.settlementMonth())
                .totalSalesAmount(amounts.totalSalesAmount())
                .totalRefundAmount(amounts.totalRefundAmount())
                .netSalesAmount(amounts.netSalesAmount())
                .platformFeeAmount(amounts.platformFeeAmount())
                .settlementAmount(amounts.settlementAmount())
                .feeRate(SettlementPolicy.DEFAULT_FEE_RATE)
                .saleCount(Math.toIntExact(amounts.saleCount()))
                .cancelCount(Math.toIntExact(amounts.cancelCount()))
                .build());

        return SettlementResult.from(settlement);
    }

    public SettlementResult confirmSettlement(@NotBlank(message = "정산 ID는 필수입니다.") String settlementId) {
        Settlement settlement = findSettlement(settlementId);
        settlement.confirm(nowInKst());
        return SettlementResult.from(settlement);
    }

    public SettlementResult markSettlementPaid(@NotBlank(message = "정산 ID는 필수입니다.") String settlementId) {
        Settlement settlement = findSettlement(settlementId);
        settlement.markPaid(nowInKst());
        return SettlementResult.from(settlement);
    }

    private Settlement findSettlement(String settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("정산 내역을 찾을 수 없습니다: " + settlementId));
    }

    private String resolveSettlementId(String candidate) {
        return candidate == null || candidate.isBlank() ? "settlement-" + UUID.randomUUID() : candidate;
    }

    private void validateCreatableSettlementMonth(YearMonth settlementMonth) {
        YearMonth currentMonth = kstClock.currentYearMonth();
        if (!settlementMonth.isBefore(currentMonth)) {
            throw new BusinessRuleViolationException("정산 생성은 이전 월에 대해서만 가능합니다.");
        }
    }

    private OffsetDateTime nowInKst() {
        return kstClock.now();
    }
}
