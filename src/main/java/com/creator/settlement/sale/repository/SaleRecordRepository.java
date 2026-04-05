package com.creator.settlement.sale.repository;

import com.creator.settlement.sale.domain.SaleRecord;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"cancellations"})
    @Query("""
            select s
            from SaleRecord s
            where s.id = :saleId
            """)
    Optional<SaleRecord> findByIdForUpdate(@Param("saleId") String saleId);

    @EntityGraph(attributePaths = {"course", "course.creator", "cancellations"})
    @Query("""
            select distinct s
            from SaleRecord s
            join s.course c
            join c.creator cr
            where cr.id = :creatorId
              and (:startAt is null or s.paidAt >= :startAt)
              and (:endExclusive is null or s.paidAt < :endExclusive)
            order by s.paidAt desc
            """)
    List<SaleRecord> findSaleRecordsByCreatorAndPaidAtRange(
            @Param("creatorId") String creatorId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endExclusive") OffsetDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"course", "course.creator"})
    List<SaleRecord> findAllByCourseCreatorIdAndPaidAtGreaterThanEqualAndPaidAtLessThan(
            String creatorId,
            OffsetDateTime startAt,
            OffsetDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"course", "course.creator"})
    List<SaleRecord> findAllByPaidAtGreaterThanEqualAndPaidAtLessThan(
            OffsetDateTime startAt,
            OffsetDateTime endExclusive
    );
}
