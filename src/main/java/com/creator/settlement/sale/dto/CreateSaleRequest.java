package com.creator.settlement.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSaleRequest(
        String saleId,
        String courseId,
        String studentId,
        BigDecimal amount,
        OffsetDateTime paidAt
) {

    public RegisterSaleCommand toCommand() {
        return new RegisterSaleCommand(saleId, courseId, studentId, amount, paidAt);
    }
}
