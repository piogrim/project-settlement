package com.creator.settlement.settlement.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record RegisterSettlementFeeRateCommand(
        String settlementFeeRateId,
        YearMonth effectiveFrom,
        BigDecimal feeRate
) {
}
