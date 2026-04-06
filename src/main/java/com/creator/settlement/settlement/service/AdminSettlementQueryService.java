package com.creator.settlement.settlement.service;

import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.settlement.domain.DailySettlementAggregate;
import com.creator.settlement.settlement.dto.query.SettlementPeriodSummaryQuery;
import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryResult;
import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryItem;
import com.creator.settlement.settlement.repository.DailySettlementAggregateRepository;
import com.creator.settlement.settlement.support.AdminSettlementQueryAccumulator;
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
    private final DailySettlementAggregateRepository dailySettlementAggregateRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementFeeRateResolver settlementFeeRateResolver;

    public SettlementPeriodSummaryResult getAdminSettlementSummary(
            @NotNull @Valid SettlementPeriodSummaryQuery query
    ) {
        Map<String, AdminSettlementQueryAccumulator> accumulators = initializeAccumulators(
                creatorRepository.findAllByOrderByIdAsc()
        );

        accumulateDailyAggregates(
                accumulators,
                dailySettlementAggregateRepository.findAllInAggregateDateRange(
                        query.startDate(),
                        query.endDate()
                )
        );

        List<SettlementPeriodSummaryItem> items = buildSummaryItems(accumulators);
        BigDecimal totalSettlementAmount = calculateTotalSettlementAmount(items);

        return new SettlementPeriodSummaryResult(query.startDate(), query.endDate(), items, totalSettlementAmount);
    }

    private Map<String, AdminSettlementQueryAccumulator> initializeAccumulators(List<Creator> creators) {
        Map<String, AdminSettlementQueryAccumulator> accumulators = new LinkedHashMap<>();
        for (Creator creator : creators) {
            accumulators.put(
                    creator.getId(),
                    new AdminSettlementQueryAccumulator(creator.getId(), creator.getName())
            );
        }
        return accumulators;
    }

    private void accumulateDailyAggregates(
            Map<String, AdminSettlementQueryAccumulator> accumulators,
            List<DailySettlementAggregate> dailyAggregates
    ) {
        for (DailySettlementAggregate dailyAggregate : dailyAggregates) {
            String creatorId = dailyAggregate.getCreator().getId();
            AdminSettlementQueryAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                accumulator.addMonthlyTotals(
                        YearMonth.from(dailyAggregate.getAggregateDate()),
                        dailyAggregate.getTotalSalesAmount(),
                        dailyAggregate.getTotalRefundAmount(),
                        dailyAggregate.getSaleCount(),
                        dailyAggregate.getCancelCount()
                );
            }
        }
    }

    private List<SettlementPeriodSummaryItem> buildSummaryItems(
            Map<String, AdminSettlementQueryAccumulator> accumulators
    ) {
        return accumulators.values().stream()
                .map(accumulator -> accumulator.toItem(settlementCalculator, settlementFeeRateResolver))
                .toList();
    }

    private BigDecimal calculateTotalSettlementAmount(List<SettlementPeriodSummaryItem> items) {
        return items.stream()
                .map(SettlementPeriodSummaryItem::settlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
