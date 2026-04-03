package com.creator.settlement.settlement.dto;

import com.creator.settlement.settlement.domain.SettlementFeeRate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementFeeRateResult(
        String settlementFeeRateId,
        String effectiveFrom,
        BigDecimal feeRate,
        OffsetDateTime createdAt
) {

    public static SettlementFeeRateResult from(SettlementFeeRate settlementFeeRate) {
        return new SettlementFeeRateResult(
                settlementFeeRate.getId(),
                settlementFeeRate.getEffectiveFrom().toString(),
                settlementFeeRate.getFeeRate(),
                settlementFeeRate.getCreatedAt()
        );
    }
}
