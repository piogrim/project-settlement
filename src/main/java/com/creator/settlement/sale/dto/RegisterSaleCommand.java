package com.creator.settlement.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RegisterSaleCommand(
        String saleId,
        String courseId,
        String studentId,
        BigDecimal amount,
        OffsetDateTime paidAt
) {
}
