package com.creator.settlement.sale.dto;

import com.creator.settlement.sale.domain.SaleCancellation;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleCancellationResult(
        String cancellationId,
        String saleId,
        BigDecimal refundAmount,
        OffsetDateTime canceledAt,
        BigDecimal totalRefundedAmount
) {

    public static SaleCancellationResult from(SaleCancellation cancellation, BigDecimal totalRefundedAmount) {
        return new SaleCancellationResult(
                cancellation.getId(),
                cancellation.getSaleRecord().getId(),
                cancellation.getRefundAmount(),
                cancellation.getCanceledAt(),
                totalRefundedAmount
        );
    }
}
