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
import com.creator.settlement.settlement.domain.MonthlySettlement;
import com.creator.settlement.settlement.dto.response.CreatorMonthlySettlementDetailResult;
import com.creator.settlement.settlement.repository.MonthlySettlementRepository;
import com.creator.settlement.settlement.support.SettlementCalculator;
import com.creator.settlement.settlement.support.SettlementFeeRateResolver;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorMonthlySettlementQueryService {

    private final CreatorRepository creatorRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final SaleCancellationRepository saleCancellationRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementFeeRateResolver settlementFeeRateResolver;
    private final KstPeriodResolver kstPeriodResolver;
    private final KstClock kstClock;

    public CreatorMonthlySettlementDetailResult getCreatorMonthlySettlement(
            @NotBlank(message = "크리에이터 ID는 필수입니다.") String creatorId,
            @NotNull(message = "조회 연월은 필수입니다.") YearMonth settlementMonth
    ) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + creatorId));

        if (isClosedMonth(settlementMonth)) {
            return monthlySettlementRepository.findByCreatorIdAndSettlementMonth(creatorId, settlementMonth)
                    .map(this::toResult)
                    .orElseThrow(() -> new BusinessRuleViolationException(
                            "이전 월 정산을 먼저 생성해야 조회할 수 있습니다."
                    ));
        }

        return calculateMonthlySettlement(creator, settlementMonth);
    }

    private boolean isClosedMonth(YearMonth settlementMonth) {
        return settlementMonth.isBefore(kstClock.currentYearMonth());
    }

    private CreatorMonthlySettlementDetailResult calculateMonthlySettlement(Creator creator, YearMonth settlementMonth) {
        KstPeriodResolver.KstRange monthlyRange = kstPeriodResolver.monthlyRange(settlementMonth);
        List<SaleRecord> saleRecords = saleRecordRepository
                .findAllForCreatorInPaidAtRange(
                        creator.getId(),
                        monthlyRange.startAt(),
                        monthlyRange.endExclusive()
                );
        List<SaleCancellation> saleCancellations = saleCancellationRepository
                .findAllForCreatorInCanceledAtRange(
                        creator.getId(),
                        monthlyRange.startAt(),
                        monthlyRange.endExclusive()
                );

        BigDecimal feeRate = settlementFeeRateResolver.resolve(settlementMonth);
        SettlementCalculator.SettlementAmounts amounts =
                settlementCalculator.calculate(saleRecords, saleCancellations, feeRate);

        return new CreatorMonthlySettlementDetailResult(
                creator.getId(),
                creator.getName(),
                settlementMonth.toString(),
                amounts.totalSalesAmount(),
                amounts.totalRefundAmount(),
                amounts.netSalesAmount(),
                amounts.platformFeeAmount(),
                amounts.settlementAmount(),
                amounts.saleCount(),
                amounts.cancelCount()
        );
    }

    private CreatorMonthlySettlementDetailResult toResult(MonthlySettlement settlement) {
        return new CreatorMonthlySettlementDetailResult(
                settlement.getCreator().getId(),
                settlement.getCreator().getName(),
                settlement.getSettlementMonth().toString(),
                settlement.getTotalSalesAmount(),
                settlement.getTotalRefundAmount(),
                settlement.getNetSalesAmount(),
                settlement.getPlatformFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getSaleCount(),
                settlement.getCancelCount()
        );
    }
}
