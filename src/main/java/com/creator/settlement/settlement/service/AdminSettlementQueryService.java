package com.creator.settlement.settlement.service;

import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.common.time.KstPeriodResolver;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.repository.SaleCancellationRepository;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import com.creator.settlement.settlement.dto.AdminSettlementSummaryQuery;
import com.creator.settlement.settlement.dto.AdminSettlementSummaryResult;
import com.creator.settlement.settlement.dto.CreatorSettlementSummaryItem;
import com.creator.settlement.settlement.support.AdminSettlementAccumulator;
import com.creator.settlement.settlement.support.SettlementCalculator;
import com.creator.settlement.settlement.support.SettlementFeeRateResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSettlementQueryService {

    private final CreatorRepository creatorRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final SaleCancellationRepository saleCancellationRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementFeeRateResolver settlementFeeRateResolver;
    private final KstPeriodResolver kstPeriodResolver;
    private final KstClock kstClock;

    public AdminSettlementSummaryResult getAdminSettlementSummary(
            @NotNull @Valid AdminSettlementSummaryQuery query
    ) {
        KstPeriodResolver.KstRange range =
                kstPeriodResolver.dateRange(query.startDate(), query.endDate());
        Map<String, AdminSettlementAccumulator> accumulators = initializeAccumulators(
                creatorRepository.findAllByOrderByIdAsc()
        );

        accumulateSales(accumulators, range);
        accumulateCancellations(accumulators, range);

        List<CreatorSettlementSummaryItem> items = buildSummaryItems(accumulators);
        BigDecimal totalSettlementAmount = calculateTotalSettlementAmount(items);

        return new AdminSettlementSummaryResult(query.startDate(), query.endDate(), items, totalSettlementAmount);
    }

    private Map<String, AdminSettlementAccumulator> initializeAccumulators(List<Creator> creators) {
        Map<String, AdminSettlementAccumulator> accumulators = new LinkedHashMap<>();
        for (Creator creator : creators) {
            accumulators.put(
                    creator.getId(),
                    new AdminSettlementAccumulator(creator.getId(), creator.getName())
            );
        }
        return accumulators;
    }

    private void accumulateSales(
            Map<String, AdminSettlementAccumulator> accumulators,
            KstPeriodResolver.KstRange range
    ) {
        List<SaleRecord> saleRecords = saleRecordRepository.findAllByPaidAtGreaterThanEqualAndPaidAtLessThan(
                range.startAt(),
                range.endExclusive()
        );

        for (SaleRecord saleRecord : saleRecords) {
            String creatorId = saleRecord.getCourse().getCreator().getId();
            AdminSettlementAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                accumulator.addSale(
                        YearMonth.from(saleRecord.getPaidAt().atZoneSameInstant(kstClock.zoneId())),
                        saleRecord.getAmount()
                );
            }
        }
    }

    private void accumulateCancellations(
            Map<String, AdminSettlementAccumulator> accumulators,
            KstPeriodResolver.KstRange range
    ) {
        List<SaleCancellation> saleCancellations = saleCancellationRepository
                .findAllByCanceledAtGreaterThanEqualAndCanceledAtLessThan(range.startAt(), range.endExclusive());

        for (SaleCancellation saleCancellation : saleCancellations) {
            String creatorId = saleCancellation.getSaleRecord().getCourse().getCreator().getId();
            AdminSettlementAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                accumulator.addCancellation(
                        YearMonth.from(saleCancellation.getCanceledAt().atZoneSameInstant(kstClock.zoneId())),
                        saleCancellation.getRefundAmount()
                );
            }
        }
    }

    private List<CreatorSettlementSummaryItem> buildSummaryItems(
            Map<String, AdminSettlementAccumulator> accumulators
    ) {
        return accumulators.values().stream()
                .map(accumulator -> accumulator.toItem(settlementCalculator, settlementFeeRateResolver))
                .toList();
    }

    private BigDecimal calculateTotalSettlementAmount(List<CreatorSettlementSummaryItem> items) {
        return items.stream()
                .map(CreatorSettlementSummaryItem::settlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
