package com.creator.settlement.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminSettlementSummaryResult(
        LocalDate startDate,
        LocalDate endDate,
        List<CreatorSettlementSummaryItem> items,
        BigDecimal totalSettlementAmount
) {
}
