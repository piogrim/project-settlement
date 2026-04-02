package com.creator.settlement.sale.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "sale_cancellations",
        indexes = {
                @Index(name = "idx_sale_cancel_canceled_at", columnList = "canceled_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleCancellation {

    @Id
    @Column(name = "cancel_id", nullable = false, updatable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleRecord saleRecord;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal refundAmount;

    @Column(name = "canceled_at", nullable = false)
    private OffsetDateTime canceledAt;

    @Builder
    private SaleCancellation(
            String id,
            SaleRecord saleRecord,
            BigDecimal refundAmount,
            OffsetDateTime canceledAt
    ) {
        this.id = id;
        this.saleRecord = saleRecord;
        this.refundAmount = refundAmount;
        this.canceledAt = canceledAt;
    }

    void attachTo(SaleRecord saleRecord) {
        this.saleRecord = saleRecord;
    }
}
