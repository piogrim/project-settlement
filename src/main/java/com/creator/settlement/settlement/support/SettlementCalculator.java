package com.creator.settlement.settlement.support;

import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementCalculator {

    public SettlementAmounts calculate(
            List<SaleRecord> saleRecords,
            List<SaleCancellation> saleCancellations,
            BigDecimal feeRate
    ) {
        BigDecimal totalSalesAmount = saleRecords.stream()
                .map(SaleRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefundAmount = saleCancellations.stream()
                .map(SaleCancellation::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return calculate(totalSalesAmount, totalRefundAmount, saleRecords.size(), saleCancellations.size(), feeRate);
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
