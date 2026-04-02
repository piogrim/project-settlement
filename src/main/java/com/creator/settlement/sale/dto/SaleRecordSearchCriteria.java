package com.creator.settlement.sale.dto;

import com.creator.settlement.common.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@ValidDateRange(startField = "startDate", endField = "endDate")
public record SaleRecordSearchCriteria(
        @NotBlank(message = "크리에이터 ID는 필수입니다.") String creatorId,
        LocalDate startDate,
        LocalDate endDate
) {
}
