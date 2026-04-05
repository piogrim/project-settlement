package com.creator.settlement.settlement.dto.response;

import java.math.BigDecimal;

public record CreatorMonthlySettlementResult(
        String creatorId,
        String creatorName,
        String settlementMonth,
        BigDecimal totalSalesAmount,
        BigDecimal totalRefundAmount,
        BigDecimal netSalesAmount,
        BigDecimal platformFeeAmount,
        BigDecimal settlementAmount,
        long saleCount,
        long cancelCount
) {
}
