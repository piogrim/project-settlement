package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.SettlementFeeRate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementFeeRateRepository extends JpaRepository<SettlementFeeRate, String> {

    boolean existsByEffectiveFrom(YearMonth effectiveFrom);

    Optional<SettlementFeeRate> findFirstByEffectiveFromLessThanEqualOrderByEffectiveFromDesc(YearMonth effectiveFrom);

    List<SettlementFeeRate> findAllByOrderByEffectiveFromDesc();
}
