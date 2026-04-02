package com.creator.settlement.settlement.dto;

import com.creator.settlement.common.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@ValidDateRange(startField = "startDate", endField = "endDate")
public record AdminSettlementSummaryQuery(
        @NotNull(message = "시작일은 필수입니다.") LocalDate startDate,
        @NotNull(message = "종료일은 필수입니다.") LocalDate endDate
) {
}
