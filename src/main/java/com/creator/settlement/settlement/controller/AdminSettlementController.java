package com.creator.settlement.settlement.controller;

import com.creator.settlement.settlement.dto.AdminSettlementSummaryQuery;
import com.creator.settlement.settlement.dto.AdminSettlementSummaryResult;
import com.creator.settlement.settlement.dto.SettlementResult;
import com.creator.settlement.settlement.service.AdminSettlementQueryService;
import com.creator.settlement.settlement.service.SettlementCommandService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final AdminSettlementQueryService adminSettlementQueryService;
    private final SettlementCommandService settlementCommandService;

    @GetMapping
    public AdminSettlementSummaryResult getSettlementSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return adminSettlementQueryService.getAdminSettlementSummary(
                new AdminSettlementSummaryQuery(startDate, endDate)
        );
    }

    @PostMapping("/{settlementId}/confirm")
    public SettlementResult confirmSettlement(@PathVariable String settlementId) {
        return settlementCommandService.confirmSettlement(settlementId);
    }

    @PostMapping("/{settlementId}/pay")
    public SettlementResult markSettlementPaid(@PathVariable String settlementId) {
        return settlementCommandService.markSettlementPaid(settlementId);
    }
}
