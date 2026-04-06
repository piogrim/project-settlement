package com.creator.settlement.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSaleCancellationCommand(
        String cancellationId,
        String saleId,
        BigDecimal refundAmount,
        OffsetDateTime canceledAt
) {
}
