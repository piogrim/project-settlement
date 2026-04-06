package com.creator.settlement.settlement.service;

import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.settlement.domain.DailySettlement;
import com.creator.settlement.settlement.dto.query.SettlementPeriodSummaryQuery;
import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryResult;
import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryItem;
import com.creator.settlement.settlement.repository.DailySettlementRepository;
import com.creator.settlement.settlement.support.DailySettlementQueryAccumulator;
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

//운영자용 기간 집계 조회 서비스

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailySettlementQueryService {

    private final CreatorRepository creatorRepository;
    private final DailySettlementRepository dailySettlementRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementFeeRateResolver settlementFeeRateResolver;

    public SettlementPeriodSummaryResult getAdminSettlementSummary(
            @NotNull @Valid SettlementPeriodSummaryQuery query
    ) {
        // 데이터가 없는 크리에이터도 결과에 포함하기 위해 누적기를 먼저 준비한다.
        Map<String, DailySettlementQueryAccumulator> accumulators = initializeAccumulators(
                creatorRepository.findAllByOrderByIdAsc()
        );

        // 일별 집계를 월별로 다시 묶어, 각 월의 수수료율을 적용할 수 있게 한다.
        accumulateDailySettlements(
                accumulators,
                dailySettlementRepository.findAllInSettlementDateRange(
                        query.startDate(),
                        query.endDate()
                )
        );

        // 크리에이터별 누적 결과를 실제 응답 아이템으로 변환한다.
        List<SettlementPeriodSummaryItem> items = buildSummaryItems(accumulators);
        // 응답 아이템의 정산 예정 금액을 모두 더해 전체 합계를 만든다.
        BigDecimal totalSettlementAmount = calculateTotalSettlementAmount(items);

        return new SettlementPeriodSummaryResult(query.startDate(), query.endDate(), items, totalSettlementAmount);
    }

    private Map<String, DailySettlementQueryAccumulator> initializeAccumulators(List<Creator> creators) {
        Map<String, DailySettlementQueryAccumulator> accumulators = new LinkedHashMap<>();
        for (Creator creator : creators) {
            accumulators.put(
                    creator.getId(),
                    new DailySettlementQueryAccumulator(creator.getId(), creator.getName())
            );
        }
        return accumulators;
    }

    private void accumulateDailySettlements(
            Map<String, DailySettlementQueryAccumulator> accumulators,
            List<DailySettlement> dailySettlements
    ) {
        for (DailySettlement dailySettlement : dailySettlements) {
            String creatorId = dailySettlement.getCreator().getId();
            DailySettlementQueryAccumulator accumulator = accumulators.get(creatorId);
            if (accumulator != null) {
                // 운영자 조회는 임의 기간 기준이므로, 일별 집계를 다시 월별 버킷에 누적한다.
                accumulator.addMonthlyTotals(
                        YearMonth.from(dailySettlement.getSettlementDate()),
                        dailySettlement.getTotalSalesAmount(),
                        dailySettlement.getTotalRefundAmount(),
                        dailySettlement.getSaleCount(),
                        dailySettlement.getCancelCount()
                );
            }
        }
    }

    private List<SettlementPeriodSummaryItem> buildSummaryItems(
            Map<String, DailySettlementQueryAccumulator> accumulators
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
