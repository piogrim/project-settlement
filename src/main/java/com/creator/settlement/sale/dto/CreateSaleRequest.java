package com.creator.settlement.sale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSaleRequest(
        String saleId,
        @NotBlank(message = "강의 ID는 필수입니다.") String courseId,
        @NotBlank(message = "수강생 ID는 필수입니다.") String studentId,
        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount,
        @NotNull(message = "결제 일시는 필수입니다.") OffsetDateTime paidAt
) {

    public RegisterSaleCommand toCommand() {
        return new RegisterSaleCommand(saleId, courseId, studentId, amount, paidAt);
    }
}
