package com.creator.settlement.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSaleCancellationRequest(
        String cancellationId,
        BigDecimal refundAmount,
        OffsetDateTime canceledAt
) {

    public RegisterSaleCancellationCommand toCommand(String saleId) {
        return new RegisterSaleCancellationCommand(cancellationId, saleId, refundAmount, canceledAt);
    }
}
