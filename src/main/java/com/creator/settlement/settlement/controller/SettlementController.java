package com.creator.settlement.settlement.controller;

import com.creator.settlement.common.exception.InvalidRequestException;
import com.creator.settlement.settlement.dto.CreateSettlementRequest;
import com.creator.settlement.settlement.dto.CreatorMonthlySettlementResult;
import com.creator.settlement.settlement.dto.SettlementResult;
import com.creator.settlement.settlement.service.CreatorSettlementQueryService;
import com.creator.settlement.settlement.service.SettlementCommandService;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/creators/{creatorId}/settlements")
public class SettlementController {

    private final CreatorSettlementQueryService creatorSettlementQueryService;
    private final SettlementCommandService settlementCommandService;

    @PostMapping
    public ResponseEntity<SettlementResult> registerSettlement(
            @PathVariable String creatorId,
            @Valid @RequestBody CreateSettlementRequest request
    ) {
        SettlementResult result = settlementCommandService.registerSettlement(
                request.toCommand(creatorId, parseYearMonth(request.settlementMonth()))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/monthly")
    public CreatorMonthlySettlementResult getCreatorMonthlySettlement(
            @PathVariable String creatorId,
            @RequestParam("yearMonth") String yearMonth
    ) {
        return creatorSettlementQueryService.getCreatorMonthlySettlement(creatorId, parseYearMonth(yearMonth));
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new InvalidRequestException("정산 연월은 필수입니다.");
        }
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException exception) {
            throw new InvalidRequestException("yearMonth는 yyyy-MM 형식이어야 합니다.");
        }
    }
}
