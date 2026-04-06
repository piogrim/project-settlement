package com.creator.settlement.settlement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SettlementPeriodSummaryResult(
        LocalDate startDate,
        LocalDate endDate,
        List<SettlementPeriodSummaryItem> items,
        BigDecimal totalSettlementAmount
) {
}
