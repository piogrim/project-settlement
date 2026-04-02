package com.creator.settlement.sale.repository;

import com.creator.settlement.sale.domain.SaleCancellation;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleCancellationRepository extends JpaRepository<SaleCancellation, String> {

    @EntityGraph(attributePaths = {"saleRecord", "saleRecord.course", "saleRecord.course.creator"})
    List<SaleCancellation> findAllBySaleRecordCourseCreatorIdAndCanceledAtGreaterThanEqualAndCanceledAtLessThan(
            String creatorId,
            OffsetDateTime startAt,
            OffsetDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"saleRecord", "saleRecord.course", "saleRecord.course.creator"})
    List<SaleCancellation> findAllByCanceledAtGreaterThanEqualAndCanceledAtLessThan(
            OffsetDateTime startAt,
            OffsetDateTime endExclusive
    );
}
