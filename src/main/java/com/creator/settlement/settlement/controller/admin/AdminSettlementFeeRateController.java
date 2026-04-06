package com.creator.settlement.settlement.controller.admin;

import com.creator.settlement.common.exception.InvalidRequestException;
import com.creator.settlement.settlement.dto.request.CreateSettlementFeeRateRequest;
import com.creator.settlement.settlement.dto.request.UpdateSettlementFeeRateRequest;
import com.creator.settlement.settlement.dto.response.SettlementFeeRateResult;
import com.creator.settlement.settlement.service.SettlementFeeRateCommandService;
import com.creator.settlement.settlement.service.SettlementFeeRateQueryService;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlement-fee-rates")
public class AdminSettlementFeeRateController {

    private final SettlementFeeRateCommandService settlementFeeRateCommandService;
    private final SettlementFeeRateQueryService settlementFeeRateQueryService;

    @PostMapping
    public ResponseEntity<SettlementFeeRateResult> registerSettlementFeeRate(
            @Valid @RequestBody CreateSettlementFeeRateRequest request
    ) {
        SettlementFeeRateResult result = settlementFeeRateCommandService.registerSettlementFeeRate(
                request.toCommand(parseYearMonth(request.effectiveFrom()))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<SettlementFeeRateResult> getSettlementFeeRates() {
        return settlementFeeRateQueryService.getSettlementFeeRates();
    }

    @PutMapping("/{settlementFeeRateId}")
    public SettlementFeeRateResult updateSettlementFeeRate(
            @PathVariable String settlementFeeRateId,
            @Valid @RequestBody UpdateSettlementFeeRateRequest request
    ) {
        return settlementFeeRateCommandService.updateSettlementFeeRate(
                request.toCommand(settlementFeeRateId)
        );
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new InvalidRequestException("적용 시작 연월은 필수입니다.");
        }
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException exception) {
            throw new InvalidRequestException("effectiveFrom은 yyyy-MM 형식이어야 합니다.");
        }
    }
}
