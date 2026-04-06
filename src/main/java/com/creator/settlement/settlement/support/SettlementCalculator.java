package com.creator.settlement.settlement.support;

import com.creator.settlement.settlement.domain.DailySettlement;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementCalculator {

    public SettlementAmounts calculate(List<DailySettlement> dailySettlements, BigDecimal feeRate) {
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        long saleCount = 0L;
        long cancelCount = 0L;

        for (DailySettlement dailySettlement : dailySettlements) {
            totalSalesAmount = totalSalesAmount.add(dailySettlement.getTotalSalesAmount());
            totalRefundAmount = totalRefundAmount.add(dailySettlement.getTotalRefundAmount());
            saleCount += dailySettlement.getSaleCount();
            cancelCount += dailySettlement.getCancelCount();
        }

        return calculate(
                totalSalesAmount,
                totalRefundAmount,
                saleCount,
                cancelCount,
                feeRate
        );
    }

    public SettlementAmounts calculate(
            BigDecimal totalSalesAmount,
            BigDecimal totalRefundAmount,
            long saleCount,
            long cancelCount,
            BigDecimal feeRate
    ) {
        BigDecimal netSalesAmount = totalSalesAmount.subtract(totalRefundAmount);
        BigDecimal platformFeeAmount = calculatePlatformFee(netSalesAmount, feeRate);
        BigDecimal settlementAmount = netSalesAmount.subtract(platformFeeAmount);

        return new SettlementAmounts(
                totalSalesAmount,
                totalRefundAmount,
                netSalesAmount,
                platformFeeAmount,
                settlementAmount,
                saleCount,
                cancelCount
        );
    }

    private BigDecimal calculatePlatformFee(BigDecimal netSalesAmount, BigDecimal feeRate) {
        BigDecimal feeBaseAmount = netSalesAmount.max(BigDecimal.ZERO);
        return feeBaseAmount.multiply(feeRate).setScale(0, RoundingMode.DOWN);
    }

    public record SettlementAmounts(
            BigDecimal totalSalesAmount,
            BigDecimal totalRefundAmount,
            BigDecimal netSalesAmount,
            BigDecimal platformFeeAmount,
            BigDecimal settlementAmount,
            long saleCount,
            long cancelCount
    ) {
    }
}
