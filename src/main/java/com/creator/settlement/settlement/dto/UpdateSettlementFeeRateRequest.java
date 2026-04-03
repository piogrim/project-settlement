package com.creator.settlement.settlement.dto;

import com.creator.settlement.settlement.support.SettlementPolicy;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateSettlementFeeRateRequest(
        @NotNull(message = "수수료율은 필수입니다.")
        @DecimalMin(value = SettlementPolicy.MIN_FEE_RATE_TEXT, message = SettlementPolicy.FEE_RATE_RANGE_MESSAGE)
        @DecimalMax(value = SettlementPolicy.MAX_FEE_RATE_TEXT, message = SettlementPolicy.FEE_RATE_RANGE_MESSAGE)
        BigDecimal feeRate
) {

    public UpdateSettlementFeeRateCommand toCommand(String settlementFeeRateId) {
        return new UpdateSettlementFeeRateCommand(settlementFeeRateId, feeRate);
    }
}
