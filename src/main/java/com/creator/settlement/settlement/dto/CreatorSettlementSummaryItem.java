package com.creator.settlement.settlement.dto;

import java.math.BigDecimal;

public record CreatorSettlementSummaryItem(
        String creatorId,
        String creatorName,
        BigDecimal totalSalesAmount,
        BigDecimal totalRefundAmount,
        BigDecimal netSalesAmount,
        BigDecimal platformFeeAmount,
        BigDecimal settlementAmount,
        long saleCount,
        long cancelCount
) {
}
