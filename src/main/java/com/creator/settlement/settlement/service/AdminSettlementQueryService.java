package com.creator.settlement.settlement.service;

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
import com.creator.settlement.settlement.support.SettlementCalculator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
    private final KstPeriodResolver kstPeriodResolver;

    public AdminSettlementSummaryResult getAdminSettlementSummary(
            @NotNull @Valid AdminSettlementSummaryQuery query
    ) {
        KstPeriodResolver.KstRange range =
                kstPeriodResolver.dateRange(query.startDate(), query.endDate());
        Map<String, CreatorSettlementAccumulator> accumulators = initializeAccumulators(
                creatorRepository.findAllByOrderByIdAsc()
        );

        accumulateSales(accumulators, range);
        accumulateCancellations(accumulators, range);

        List<CreatorSettlementSummaryItem> items = buildSummaryItems(accumulators);
        BigDecimal totalSettlementAmount = calculateTotalSettlementAmount(items);

        return new AdminSettlementSummaryResult(query.startDate(), query.endDate(), items, totalSettlementAmount);
    }

    private Map<String, CreatorSettlementAccumulator> initializeAccumulators(List<Creator> creators) {
        Map<String, CreatorSettlementAccumulator> accumulators = new LinkedHashMap<>();
        for (Creator creator : creators) {
            accumulators.put(
                    creator.getId(),
                    new CreatorSettlementAccumulator(creator.getId(), creator.getName())
            );
        }
        return accumulators;
    }

    private void accumulateSales(
            Map<String, CreatorSettlementAccumulator> accumulators,
            KstPeriodResolver.KstRange range
    ) {
        List<SaleRecord> saleRecords = saleRecordRepository.findAllByPaidAtGreaterThanEqualAndPaidAtLessThan(
                range.startAt(),
                range.endExclusive()
        );

        for (SaleRecord saleRecord : saleRecords) {
            String creatorId = saleRecord.getCourse().getCreator().getId();
            CreatorSettlementAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                accumulator.addSale(saleRecord.getAmount());
            }
        }
    }

    private void accumulateCancellations(
            Map<String, CreatorSettlementAccumulator> accumulators,
            KstPeriodResolver.KstRange range
    ) {
        List<SaleCancellation> saleCancellations = saleCancellationRepository
                .findAllByCanceledAtGreaterThanEqualAndCanceledAtLessThan(range.startAt(), range.endExclusive());

        for (SaleCancellation saleCancellation : saleCancellations) {
            String creatorId = saleCancellation.getSaleRecord().getCourse().getCreator().getId();
            CreatorSettlementAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                accumulator.addCancellation(saleCancellation.getRefundAmount());
            }
        }
    }

    private List<CreatorSettlementSummaryItem> buildSummaryItems(
            Map<String, CreatorSettlementAccumulator> accumulators
    ) {
        return accumulators.values().stream()
                .map(accumulator -> accumulator.toItem(settlementCalculator))
                .toList();
    }

    private BigDecimal calculateTotalSettlementAmount(List<CreatorSettlementSummaryItem> items) {
        return items.stream()
                .map(CreatorSettlementSummaryItem::settlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static final class CreatorSettlementAccumulator {
        private final String creatorId;
        private final String creatorName;
        private BigDecimal totalSalesAmount = BigDecimal.ZERO;
        private BigDecimal totalRefundAmount = BigDecimal.ZERO;
        private long saleCount;
        private long cancelCount;

        private CreatorSettlementAccumulator(String creatorId, String creatorName) {
            this.creatorId = creatorId;
            this.creatorName = creatorName;
        }

        private void addSale(BigDecimal amount) {
            totalSalesAmount = totalSalesAmount.add(amount);
            saleCount++;
        }

        private void addCancellation(BigDecimal refundAmount) {
            totalRefundAmount = totalRefundAmount.add(refundAmount);
            cancelCount++;
        }

        private CreatorSettlementSummaryItem toItem(SettlementCalculator settlementCalculator) {
            SettlementCalculator.SettlementAmounts amounts = settlementCalculator.calculate(
                    totalSalesAmount,
                    totalRefundAmount,
                    saleCount,
                    cancelCount
            );

            return new CreatorSettlementSummaryItem(
                    creatorId,
                    creatorName,
                    amounts.totalSalesAmount(),
                    amounts.totalRefundAmount(),
                    amounts.netSalesAmount(),
                    amounts.platformFeeAmount(),
                    amounts.settlementAmount(),
                    amounts.saleCount(),
                    amounts.cancelCount()
            );
        }
    }
}
