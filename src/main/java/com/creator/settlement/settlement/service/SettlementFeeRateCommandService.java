package com.creator.settlement.settlement.service;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.settlement.domain.SettlementFeeRate;
import com.creator.settlement.settlement.dto.RegisterSettlementFeeRateCommand;
import com.creator.settlement.settlement.dto.SettlementFeeRateResult;
import com.creator.settlement.settlement.dto.UpdateSettlementFeeRateCommand;
import com.creator.settlement.settlement.repository.SettlementFeeRateRepository;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementFeeRateCommandService {

    private final SettlementFeeRateRepository settlementFeeRateRepository;
    private final KstClock kstClock;

    public SettlementFeeRateResult registerSettlementFeeRate(RegisterSettlementFeeRateCommand command) {
        validateCreatableEffectiveFrom(command.effectiveFrom());

        if (settlementFeeRateRepository.existsByEffectiveFrom(command.effectiveFrom())) {
            throw new BusinessRuleViolationException(
                    "해당 적용 시작 연월의 수수료율 이력이 이미 존재합니다: " + command.effectiveFrom()
            );
        }

        String settlementFeeRateId = resolveId(command.settlementFeeRateId());
        if (settlementFeeRateRepository.existsById(settlementFeeRateId)) {
            throw new BusinessRuleViolationException("이미 존재하는 수수료율 이력 ID입니다: " + settlementFeeRateId);
        }

        SettlementFeeRate settlementFeeRate = settlementFeeRateRepository.save(SettlementFeeRate.builder()
                .id(settlementFeeRateId)
                .effectiveFrom(command.effectiveFrom())
                .feeRate(command.feeRate())
                .createdAt(kstClock.now())
                .build());

        return SettlementFeeRateResult.from(settlementFeeRate);
    }

    public SettlementFeeRateResult updateSettlementFeeRate(UpdateSettlementFeeRateCommand command) {
        SettlementFeeRate settlementFeeRate = settlementFeeRateRepository.findById(command.settlementFeeRateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "수수료율 이력을 찾을 수 없습니다: " + command.settlementFeeRateId()
                ));

        validateUpdatableEffectiveFrom(settlementFeeRate.getEffectiveFrom());
        settlementFeeRate.changeFeeRate(command.feeRate());

        return SettlementFeeRateResult.from(settlementFeeRate);
    }

    private String resolveId(String candidate) {
        return candidate == null || candidate.isBlank()
                ? "settlement-fee-rate-" + UUID.randomUUID()
                : candidate;
    }

    private void validateUpdatableEffectiveFrom(YearMonth effectiveFrom) {
        YearMonth currentMonth = kstClock.currentYearMonth();
        if (effectiveFrom.isBefore(currentMonth)) {
            throw new BusinessRuleViolationException("과거 월에 적용되는 수수료율 이력은 수정할 수 없습니다.");
        }
    }

    private void validateCreatableEffectiveFrom(YearMonth effectiveFrom) {
        YearMonth currentMonth = kstClock.currentYearMonth();
        if (effectiveFrom.isBefore(currentMonth)) {
            throw new BusinessRuleViolationException("과거 월에 적용되는 수수료율 이력은 생성할 수 없습니다.");
        }
    }
}
