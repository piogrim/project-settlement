package com.creator.settlement.settlement.dto.response;

import com.creator.settlement.settlement.domain.SettlementFeeRate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementFeeRateHistoryResult(
        String settlementFeeRateId,
        String effectiveFrom,
        BigDecimal feeRate,
        OffsetDateTime createdAt
) {

    public static SettlementFeeRateHistoryResult from(SettlementFeeRate settlementFeeRate) {
        return new SettlementFeeRateHistoryResult(
                settlementFeeRate.getId(),
                settlementFeeRate.getEffectiveFrom().toString(),
                settlementFeeRate.getFeeRate(),
                settlementFeeRate.getCreatedAt()
        );
    }
}
