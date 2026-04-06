package com.creator.settlement.settlement.dto.command;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CreateSettlementFeeRateCommand(
        String settlementFeeRateId,
        YearMonth effectiveFrom,
        BigDecimal feeRate
) {
}
