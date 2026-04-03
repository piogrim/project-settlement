package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.Settlement;
import java.time.YearMonth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

    boolean existsByCreator_IdAndSettlementMonth(String creatorId, YearMonth settlementMonth);

    Optional<Settlement> findByCreator_IdAndSettlementMonth(String creatorId, YearMonth settlementMonth);
}
