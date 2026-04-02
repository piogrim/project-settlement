package com.creator.settlement.settlement.controller;

import com.creator.settlement.common.exception.InvalidRequestException;
import com.creator.settlement.settlement.dto.CreatorMonthlySettlementResult;
import com.creator.settlement.settlement.service.CreatorSettlementQueryService;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/creators/{creatorId}/settlements")
public class SettlementController {

    private final CreatorSettlementQueryService creatorSettlementQueryService;

    @GetMapping("/monthly")
    public CreatorMonthlySettlementResult getCreatorMonthlySettlement(
            @PathVariable String creatorId,
            @RequestParam("yearMonth") String yearMonth
    ) {
        return creatorSettlementQueryService.getCreatorMonthlySettlement(creatorId, parseYearMonth(yearMonth));
    }

    private YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException exception) {
            throw new InvalidRequestException("yearMonth는 yyyy-MM 형식이어야 합니다.");
        }
    }
}
