package com.creator.settlement.settlement.support;

import com.creator.settlement.settlement.dto.response.CreatorSettlementSummaryItem;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminSettlementAccumulator {

    private final String creatorId;
    private final String creatorName;
    private final Map<YearMonth, MonthlySettlementAccumulator> monthlyAccumulators = new LinkedHashMap<>();

    public AdminSettlementAccumulator(String creatorId, String creatorName) {
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    public void addSale(YearMonth settlementMonth, BigDecimal amount) {
        monthlyAccumulator(settlementMonth).addSale(amount);
    }

    public void addCancellation(YearMonth settlementMonth, BigDecimal refundAmount) {
        monthlyAccumulator(settlementMonth).addCancellation(refundAmount);
    }

    public CreatorSettlementSummaryItem toItem(
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

        for (Map.Entry<YearMonth, MonthlySettlementAccumulator> entry : monthlyAccumulators.entrySet()) {
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

        return new CreatorSettlementSummaryItem(
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

    private MonthlySettlementAccumulator monthlyAccumulator(YearMonth settlementMonth) {
        return monthlyAccumulators.computeIfAbsent(settlementMonth, ignored -> new MonthlySettlementAccumulator());
    }

    private static final class MonthlySettlementAccumulator {
        private BigDecimal totalSalesAmount = BigDecimal.ZERO;
        private BigDecimal totalRefundAmount = BigDecimal.ZERO;
        private long saleCount;
        private long cancelCount;

        private void addSale(BigDecimal amount) {
            totalSalesAmount = totalSalesAmount.add(amount);
            saleCount++;
        }

        private void addCancellation(BigDecimal refundAmount) {
            totalRefundAmount = totalRefundAmount.add(refundAmount);
            cancelCount++;
        }
    }
}
