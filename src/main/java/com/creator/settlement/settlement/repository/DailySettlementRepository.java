package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.DailySettlement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, String> {

    @Query("""
            select dailySettlement
            from DailySettlement dailySettlement
            where dailySettlement.creator.id = :creatorId
              and dailySettlement.settlementDate = :settlementDate
            """)
    Optional<DailySettlement> findByCreatorIdAndSettlementDate(
            @Param("creatorId") String creatorId,
            @Param("settlementDate") LocalDate settlementDate
    );

    @Query("""
            select dailySettlement
            from DailySettlement dailySettlement
            where dailySettlement.creator.id = :creatorId
              and dailySettlement.settlementDate >= :startDate
              and dailySettlement.settlementDate <= :endDate
            order by dailySettlement.settlementDate asc
            """)
    List<DailySettlement> findAllForCreatorInSettlementDateRange(
            @Param("creatorId") String creatorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @EntityGraph(attributePaths = {"creator"})
    @Query("""
            select dailySettlement
            from DailySettlement dailySettlement
            where dailySettlement.settlementDate >= :startDate
              and dailySettlement.settlementDate <= :endDate
            order by dailySettlement.creator.id asc, dailySettlement.settlementDate asc
            """)
    List<DailySettlement> findAllInSettlementDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
