package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.SettlementFeeRate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementFeeRateRepository extends JpaRepository<SettlementFeeRate, String> {

    boolean existsByEffectiveFrom(YearMonth effectiveFrom);

    @Query("""
            select settlementFeeRate
            from SettlementFeeRate settlementFeeRate
            where settlementFeeRate.effectiveFrom = (
                select max(candidate.effectiveFrom)
                from SettlementFeeRate candidate
                where candidate.effectiveFrom <= :settlementMonth
            )
            """)
    Optional<SettlementFeeRate> findLatestApplicableRate(@Param("settlementMonth") YearMonth settlementMonth);

    List<SettlementFeeRate> findAllByOrderByEffectiveFromDesc();
}
