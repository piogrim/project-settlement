package com.creator.settlement.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RegisterSaleCancellationCommand(
        String cancellationId,
        String saleId,
        BigDecimal refundAmount,
        OffsetDateTime canceledAt
) {
}
