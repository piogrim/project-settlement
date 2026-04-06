package com.creator.settlement.settlement.dto.query;

import com.creator.settlement.common.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@ValidDateRange(startField = "startDate", endField = "endDate")
public record SettlementPeriodSummaryQuery(
        @NotNull(message = "?쒖옉?쇱? ?꾩닔?낅땲??") LocalDate startDate,
        @NotNull(message = "醫낅즺?쇱? ?꾩닔?낅땲??") LocalDate endDate
) {
}
