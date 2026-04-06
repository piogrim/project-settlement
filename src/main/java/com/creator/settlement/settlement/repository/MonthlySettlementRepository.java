package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.MonthlySettlement;
import java.time.YearMonth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, String> {

    @Query("""
            select case when count(settlement) > 0 then true else false end
            from MonthlySettlement settlement
            where settlement.creator.id = :creatorId
              and settlement.settlementMonth = :settlementMonth
            """)
    boolean existsByCreatorIdAndSettlementMonth(
            @Param("creatorId") String creatorId,
            @Param("settlementMonth") YearMonth settlementMonth
    );

    @Query("""
            select settlement
            from MonthlySettlement settlement
            where settlement.creator.id = :creatorId
              and settlement.settlementMonth = :settlementMonth
            """)
    Optional<MonthlySettlement> findByCreatorIdAndSettlementMonth(
            @Param("creatorId") String creatorId,
            @Param("settlementMonth") YearMonth settlementMonth
    );
}
