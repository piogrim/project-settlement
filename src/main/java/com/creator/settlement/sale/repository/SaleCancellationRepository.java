package com.creator.settlement.sale.repository;

import com.creator.settlement.sale.domain.SaleCancellation;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleCancellationRepository extends JpaRepository<SaleCancellation, String> {

    @EntityGraph(attributePaths = {"saleRecord", "saleRecord.course", "saleRecord.course.creator"})
    @Query("""
            select cancellation
            from SaleCancellation cancellation
            join cancellation.saleRecord saleRecord
            join saleRecord.course course
            join course.creator creator
            where creator.id = :creatorId
              and cancellation.canceledAt >= :startAt
              and cancellation.canceledAt < :endExclusive
            """)
    List<SaleCancellation> findAllForCreatorInCanceledAtRange(
            @Param("creatorId") String creatorId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endExclusive") OffsetDateTime endExclusive
    );
}
