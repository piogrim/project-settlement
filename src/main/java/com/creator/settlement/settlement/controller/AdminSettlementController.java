package com.creator.settlement.settlement.controller;

import com.creator.settlement.settlement.dto.AdminSettlementSummaryQuery;
import com.creator.settlement.settlement.dto.AdminSettlementSummaryResult;
import com.creator.settlement.settlement.dto.SettlementResult;
import com.creator.settlement.settlement.service.AdminSettlementQueryService;
import com.creator.settlement.settlement.service.SettlementCommandService;
import com.creator.settlement.settlement.support.SettlementCsvExporter;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final SettlementCsvExporter settlementCsvExporter;

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

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportSettlementSummaryAsCsv(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        AdminSettlementSummaryResult summary = adminSettlementQueryService.getAdminSettlementSummary(
                new AdminSettlementSummaryQuery(startDate, endDate)
        );
        String csv = settlementCsvExporter.export(summary);

        String fileName = "settlement-summary-%s-to-%s.csv".formatted(startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
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
