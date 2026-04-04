package com.creator.settlement.sale.domain;

import com.creator.settlement.course.domain.Course;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "sale_records",
        indexes = {
                @Index(name = "idx_sale_paid_at", columnList = "paid_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleRecord {

    @Id
    @Column(name = "sale_id", nullable = false, updatable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt;

    @Column(name = "total_refund_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "saleRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleCancellation> cancellations = new ArrayList<>();

    @Builder
    private SaleRecord(
            String id,
            Course course,
            String studentId,
            BigDecimal amount,
            OffsetDateTime paidAt
    ) {
        this.id = id;
        this.course = course;
        this.studentId = studentId;
        this.amount = amount;
        this.paidAt = paidAt;
        this.totalRefundAmount = BigDecimal.ZERO;
    }

    public void addCancellation(SaleCancellation cancellation) {
        this.cancellations.add(cancellation);
        cancellation.attachTo(this);
        this.totalRefundAmount = this.totalRefundAmount.add(cancellation.getRefundAmount());
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount == null ? BigDecimal.ZERO : totalRefundAmount;
    }
}
