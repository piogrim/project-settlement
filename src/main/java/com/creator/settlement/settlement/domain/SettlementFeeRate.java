package com.creator.settlement.settlement.domain;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.persistence.YearMonthAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "settlement_fee_rates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_fee_rate_effective_from",
                        columnNames = {"effective_from"}
                )
        },
        indexes = {
                @Index(name = "idx_settlement_fee_rate_effective_from", columnList = "effective_from")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementFeeRate {

    @Id
    @Column(name = "settlement_fee_rate_id", nullable = false, updatable = false, length = 50)
    private String id;

    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "effective_from", nullable = false, length = 7)
    private YearMonth effectiveFrom;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal feeRate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Builder
    private SettlementFeeRate(
            String id,
            YearMonth effectiveFrom,
            BigDecimal feeRate,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.effectiveFrom = effectiveFrom;
        this.feeRate = feeRate;
        this.createdAt = createdAt;
    }

    public void changeFeeRate(BigDecimal feeRate) {
        if (feeRate == null) {
            throw new BusinessRuleViolationException("수수료율은 필수입니다.");
        }
        this.feeRate = feeRate;
    }
}
