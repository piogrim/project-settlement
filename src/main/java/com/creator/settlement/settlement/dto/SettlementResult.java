package com.creator.settlement.settlement.dto;

import com.creator.settlement.settlement.domain.Settlement;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementResult(
        String settlementId,
        String creatorId,
        String creatorName,
        String settlementMonth,
        BigDecimal totalSalesAmount,
        BigDecimal totalRefundAmount,
        BigDecimal netSalesAmount,
        BigDecimal platformFeeAmount,
        BigDecimal settlementAmount,
        BigDecimal feeRate,
        int saleCount,
        int cancelCount,
        String status,
        OffsetDateTime confirmedAt,
        OffsetDateTime paidAt
) {

    public static SettlementResult from(Settlement settlement) {
        return new SettlementResult(
                settlement.getId(),
                settlement.getCreator().getId(),
                settlement.getCreator().getName(),
                settlement.getSettlementMonth().toString(),
                settlement.getTotalSalesAmount(),
                settlement.getTotalRefundAmount(),
                settlement.getNetSalesAmount(),
                settlement.getPlatformFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getFeeRate(),
                settlement.getSaleCount(),
                settlement.getCancelCount(),
                settlement.getStatus().name(),
                settlement.getConfirmedAt(),
                settlement.getPaidAt()
        );
    }
}
