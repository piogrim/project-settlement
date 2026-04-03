package com.creator.settlement.sale.controller;

import com.creator.settlement.sale.dto.CreateSaleCancellationRequest;
import com.creator.settlement.sale.dto.CreateSaleRequest;
import com.creator.settlement.sale.dto.SaleCancellationResult;
import com.creator.settlement.sale.dto.SaleRecordResult;
import com.creator.settlement.sale.dto.SaleRecordSearchCriteria;
import com.creator.settlement.sale.service.SaleCommandService;
import com.creator.settlement.sale.service.SaleQueryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api")
public class SaleController {

    private final SaleCommandService saleCommandService;
    private final SaleQueryService saleQueryService;

    @PostMapping("/sales")
    public ResponseEntity<SaleRecordResult> registerSale(@Valid @RequestBody CreateSaleRequest request) {
        SaleRecordResult result = saleCommandService.registerSale(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/sales/{saleId}/cancellations")
    public ResponseEntity<SaleCancellationResult> registerCancellation(
            @PathVariable String saleId,
            @Valid @RequestBody CreateSaleCancellationRequest request
    ) {
        SaleCancellationResult result = saleCommandService.registerCancellation(request.toCommand(saleId));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/creators/{creatorId}/sales")
    public List<SaleRecordResult> getSaleRecords(
            @PathVariable String creatorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return saleQueryService.getSaleRecords(new SaleRecordSearchCriteria(creatorId, startDate, endDate));
    }
}
