package com.creator.settlement.settlement.support;

import com.creator.settlement.settlement.dto.response.SettlementPeriodSummaryItem;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public class DailySettlementQueryAccumulator {

    private final String creatorId;
    private final String creatorName;
    private final Map<YearMonth, MonthlyTotalsAccumulator> monthlyAccumulators = new LinkedHashMap<>();

    public DailySettlementQueryAccumulator(String creatorId, String creatorName) {
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    public void addMonthlyTotals(
            YearMonth settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal totalRefundAmount,
            long saleCount,
            long cancelCount
    ) {
        monthlyAccumulator(settlementMonth).addTotals(
                totalSalesAmount,
                totalRefundAmount,
                saleCount,
                cancelCount
        );
    }

    public SettlementPeriodSummaryItem toItem(
            SettlementCalculator settlementCalculator,
            SettlementFeeRateResolver settlementFeeRateResolver
    ) {
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal platformFeeAmount = BigDecimal.ZERO;
        BigDecimal settlementAmount = BigDecimal.ZERO;
        long saleCount = 0;
        long cancelCount = 0;

        for (Map.Entry<YearMonth, MonthlyTotalsAccumulator> entry : monthlyAccumulators.entrySet()) {
            BigDecimal feeRate = settlementFeeRateResolver.resolve(entry.getKey());
            SettlementCalculator.SettlementAmounts amounts = settlementCalculator.calculate(
                    entry.getValue().totalSalesAmount,
                    entry.getValue().totalRefundAmount,
                    entry.getValue().saleCount,
                    entry.getValue().cancelCount,
                    feeRate
            );
            totalSalesAmount = totalSalesAmount.add(amounts.totalSalesAmount());
            totalRefundAmount = totalRefundAmount.add(amounts.totalRefundAmount());
            netSalesAmount = netSalesAmount.add(amounts.netSalesAmount());
            platformFeeAmount = platformFeeAmount.add(amounts.platformFeeAmount());
            settlementAmount = settlementAmount.add(amounts.settlementAmount());
            saleCount += amounts.saleCount();
            cancelCount += amounts.cancelCount();
        }

        return new SettlementPeriodSummaryItem(
                creatorId,
                creatorName,
                totalSalesAmount,
                totalRefundAmount,
                netSalesAmount,
                platformFeeAmount,
                settlementAmount,
                saleCount,
                cancelCount
        );
    }

    private MonthlyTotalsAccumulator monthlyAccumulator(YearMonth settlementMonth) {
        return monthlyAccumulators.computeIfAbsent(settlementMonth, ignored -> new MonthlyTotalsAccumulator());
    }

    private static final class MonthlyTotalsAccumulator {
        private BigDecimal totalSalesAmount = BigDecimal.ZERO;
        private BigDecimal totalRefundAmount = BigDecimal.ZERO;
        private long saleCount;
        private long cancelCount;

        private void addTotals(
                BigDecimal totalSalesAmount,
                BigDecimal totalRefundAmount,
                long saleCount,
                long cancelCount
        ) {
            this.totalSalesAmount = this.totalSalesAmount.add(totalSalesAmount);
            this.totalRefundAmount = this.totalRefundAmount.add(totalRefundAmount);
            this.saleCount += saleCount;
            this.cancelCount += cancelCount;
        }
    }
}
