package com.creator.settlement.settlement.controller.admin;

import com.creator.settlement.settlement.dto.query.SettlementPeriodSummaryQuery;
import com.creator.settlement.settlement.dto.response.MonthlySettlementSnapshotResult;
import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryResult;
import com.creator.settlement.settlement.service.AdminSettlementQueryService;
import com.creator.settlement.settlement.service.MonthlySettlementCommandService;
import com.creator.settlement.settlement.support.AdminSettlementSummaryCsvExporter;
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
    private final MonthlySettlementCommandService monthlySettlementCommandService;
    private final AdminSettlementSummaryCsvExporter adminSettlementSummaryCsvExporter;

    @GetMapping
    public SettlementPeriodSummaryResult getSettlementSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return adminSettlementQueryService.getAdminSettlementSummary(
                new SettlementPeriodSummaryQuery(startDate, endDate)
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
        SettlementPeriodSummaryResult summary = adminSettlementQueryService.getAdminSettlementSummary(
                new SettlementPeriodSummaryQuery(startDate, endDate)
        );
        String csv = adminSettlementSummaryCsvExporter.export(summary);

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
    public MonthlySettlementSnapshotResult confirmSettlement(@PathVariable String settlementId) {
        return monthlySettlementCommandService.confirmSettlement(settlementId);
    }

    @PostMapping("/{settlementId}/pay")
    public MonthlySettlementSnapshotResult markSettlementPaid(@PathVariable String settlementId) {
        return monthlySettlementCommandService.markSettlementPaid(settlementId);
    }
}
