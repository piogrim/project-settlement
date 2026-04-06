package com.creator.settlement.settlement.dto.command;

import java.math.BigDecimal;

public record UpdateSettlementFeeRateCommand(
        String settlementFeeRateId,
        BigDecimal feeRate
) {
}
