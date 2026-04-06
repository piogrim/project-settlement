package com.creator.settlement.settlement.domain;

import com.creator.settlement.creator.domain.Creator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "creator_daily_settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_creator_daily_settlement",
                        columnNames = {"creator_id", "settlement_date"}
                )
        },
        indexes = {
                @Index(name = "idx_creator_daily_settlement_date", columnList = "settlement_date"),
                @Index(
                        name = "idx_creator_daily_settlement_creator_date",
                        columnList = "creator_id, settlement_date"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySettlement {

    @Id
    @Column(name = "daily_settlement_id", nullable = false, updatable = false, length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_sales_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalSalesAmount;

    @Column(name = "total_refund_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalRefundAmount;

    @Column(name = "sale_count", nullable = false)
    private long saleCount;

    @Column(name = "cancel_count", nullable = false)
    private long cancelCount;

    private DailySettlement(Creator creator, LocalDate settlementDate) {
        this.id = generateId(creator.getId(), settlementDate);
        this.creator = creator;
        this.settlementDate = settlementDate;
        this.totalSalesAmount = BigDecimal.ZERO;
        this.totalRefundAmount = BigDecimal.ZERO;
        this.saleCount = 0;
        this.cancelCount = 0;
    }

    public static DailySettlement create(Creator creator, LocalDate settlementDate) {
        return new DailySettlement(creator, settlementDate);
    }

    public void addSale(BigDecimal amount) {
        this.totalSalesAmount = this.totalSalesAmount.add(amount);
        this.saleCount++;
    }

    public void addCancellation(BigDecimal refundAmount) {
        this.totalRefundAmount = this.totalRefundAmount.add(refundAmount);
        this.cancelCount++;
    }

    private static String generateId(String creatorId, LocalDate settlementDate) {
        return creatorId + ":" + settlementDate;
    }
}
