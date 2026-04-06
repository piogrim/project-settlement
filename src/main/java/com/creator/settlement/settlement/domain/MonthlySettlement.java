package com.creator.settlement.settlement.domain;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.persistence.YearMonthAttributeConverter;
import com.creator.settlement.creator.domain.Creator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_creator_month",
                        columnNames = {"creator_id", "settlement_month"}
                )
        },
        indexes = {
                @Index(name = "idx_settlement_month", columnList = "settlement_month"),
                @Index(name = "idx_settlement_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlySettlement {

    @Id
    @Column(name = "settlement_id", nullable = false, updatable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "settlement_month", nullable = false, length = 7)
    private YearMonth settlementMonth;

    @Column(name = "total_sales_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalSalesAmount;

    @Column(name = "total_refund_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalRefundAmount;

    @Column(name = "net_sales_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal netSalesAmount;

    @Column(name = "platform_fee_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal platformFeeAmount;

    @Column(name = "settlement_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal settlementAmount;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal feeRate;

    @Column(name = "sale_count", nullable = false)
    private int saleCount;

    @Column(name = "cancel_count", nullable = false)
    private int cancelCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Builder
    private MonthlySettlement(
            String id,
            Creator creator,
            YearMonth settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal totalRefundAmount,
            BigDecimal netSalesAmount,
            BigDecimal platformFeeAmount,
            BigDecimal settlementAmount,
            BigDecimal feeRate,
            int saleCount,
            int cancelCount,
            SettlementStatus status,
            OffsetDateTime confirmedAt,
            OffsetDateTime paidAt
    ) {
        this.id = id;
        this.creator = creator;
        this.settlementMonth = settlementMonth;
        this.totalSalesAmount = totalSalesAmount;
        this.totalRefundAmount = totalRefundAmount;
        this.netSalesAmount = netSalesAmount;
        this.platformFeeAmount = platformFeeAmount;
        this.settlementAmount = settlementAmount;
        this.feeRate = feeRate;
        this.saleCount = saleCount;
        this.cancelCount = cancelCount;
        this.status = status == null ? SettlementStatus.PENDING : status;
        this.confirmedAt = confirmedAt;
        this.paidAt = paidAt;
    }

    public void confirm(OffsetDateTime confirmedAt) {
        if (status != SettlementStatus.PENDING) {
            throw new BusinessRuleViolationException("PENDING 상태의 정산만 확정할 수 있습니다.");
        }
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public void markPaid(OffsetDateTime paidAt) {
        if (status != SettlementStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("CONFIRMED 상태의 정산만 지급 완료로 변경할 수 있습니다.");
        }
        this.status = SettlementStatus.PAID;
        this.paidAt = paidAt;
    }
}
