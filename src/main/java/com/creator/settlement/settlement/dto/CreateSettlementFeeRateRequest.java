package com.creator.settlement.settlement.dto;

import com.creator.settlement.settlement.support.SettlementPolicy;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.YearMonth;

public record CreateSettlementFeeRateRequest(
        String settlementFeeRateId,
        @NotBlank(message = "적용 시작 연월은 필수입니다.") String effectiveFrom,
        @NotNull(message = "수수료율은 필수입니다.")
        @DecimalMin(value = SettlementPolicy.MIN_FEE_RATE_TEXT, message = SettlementPolicy.FEE_RATE_RANGE_MESSAGE)
        @DecimalMax(value = SettlementPolicy.MAX_FEE_RATE_TEXT, message = SettlementPolicy.FEE_RATE_RANGE_MESSAGE)
        BigDecimal feeRate
) {

    public RegisterSettlementFeeRateCommand toCommand(YearMonth parsedEffectiveFrom) {
        return new RegisterSettlementFeeRateCommand(settlementFeeRateId, parsedEffectiveFrom, feeRate);
    }
}
