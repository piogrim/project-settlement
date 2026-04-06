package com.creator.settlement.settlement.dto.response;

import com.creator.settlement.settlement.domain.MonthlySettlement;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MonthlySettlementSnapshotResult(
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

    public static MonthlySettlementSnapshotResult from(MonthlySettlement monthlySettlement) {
        return new MonthlySettlementSnapshotResult(
                monthlySettlement.getId(),
                monthlySettlement.getCreator().getId(),
                monthlySettlement.getCreator().getName(),
                monthlySettlement.getSettlementMonth().toString(),
                monthlySettlement.getTotalSalesAmount(),
                monthlySettlement.getTotalRefundAmount(),
                monthlySettlement.getNetSalesAmount(),
                monthlySettlement.getPlatformFeeAmount(),
                monthlySettlement.getSettlementAmount(),
                monthlySettlement.getFeeRate(),
                monthlySettlement.getSaleCount(),
                monthlySettlement.getCancelCount(),
                monthlySettlement.getStatus().name(),
                monthlySettlement.getConfirmedAt(),
                monthlySettlement.getPaidAt()
        );
    }
}
