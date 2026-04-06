package com.creator.settlement.settlement.controller.creator;

import com.creator.settlement.common.exception.InvalidRequestException;
import com.creator.settlement.settlement.dto.request.CreateMonthlySettlementRequest;
import com.creator.settlement.settlement.dto.response.CreatorMonthlySettlementDetailResult;
import com.creator.settlement.settlement.dto.response.MonthlySettlementSnapshotResult;
import com.creator.settlement.settlement.service.MonthlySettlementQueryService;
import com.creator.settlement.settlement.service.MonthlySettlementCommandService;
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
public class CreatorSettlementController {

    private final MonthlySettlementQueryService monthlySettlementQueryService;
    private final MonthlySettlementCommandService monthlySettlementCommandService;

    @PostMapping
    public ResponseEntity<MonthlySettlementSnapshotResult> createSettlement(
            @PathVariable String creatorId,
            @Valid @RequestBody CreateMonthlySettlementRequest request
    ) {
        MonthlySettlementSnapshotResult result = monthlySettlementCommandService.createSettlement(
                request.toCommand(creatorId, parseYearMonth(request.settlementMonth()))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/monthly")
    public CreatorMonthlySettlementDetailResult getCreatorMonthlySettlement(
            @PathVariable String creatorId,
            @RequestParam("yearMonth") String yearMonth
    ) {
        return monthlySettlementQueryService.getCreatorMonthlySettlement(creatorId, parseYearMonth(yearMonth));
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
