package com.creator.settlement.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSaleCancellationRequest(
        String cancellationId,
        @NotNull(message = "환불 금액은 필수입니다.")
        @Positive(message = "환불 금액은 0보다 커야 합니다.")
        BigDecimal refundAmount,
        @NotNull(message = "취소 일시는 필수입니다.") OffsetDateTime canceledAt
) {

    public RegisterSaleCancellationCommand toCommand(String saleId) {
        return new RegisterSaleCancellationCommand(cancellationId, saleId, refundAmount, canceledAt);
    }
}
