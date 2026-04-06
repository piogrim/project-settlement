package com.creator.settlement.settlement.repository;

import com.creator.settlement.settlement.domain.DailySettlementAggregate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailySettlementAggregateRepository extends JpaRepository<DailySettlementAggregate, String> {

    @Query("""
            select aggregate
            from DailySettlementAggregate aggregate
            where aggregate.creator.id = :creatorId
              and aggregate.aggregateDate = :aggregateDate
            """)
    Optional<DailySettlementAggregate> findByCreatorIdAndAggregateDate(
            @Param("creatorId") String creatorId,
            @Param("aggregateDate") LocalDate aggregateDate
    );

    @EntityGraph(attributePaths = {"creator"})
    @Query("""
            select aggregate
            from DailySettlementAggregate aggregate
            where aggregate.aggregateDate >= :startDate
              and aggregate.aggregateDate <= :endDate
            order by aggregate.creator.id asc, aggregate.aggregateDate asc
            """)
    List<DailySettlementAggregate> findAllInAggregateDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
