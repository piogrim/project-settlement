package com.creator.settlement.sale.dto;

import com.creator.settlement.course.domain.Course;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.sale.domain.SaleRecord;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleRecordResult(
        String saleId,
        String courseId,
        String courseTitle,
        String creatorId,
        String creatorName,
        String studentId,
        BigDecimal amount,
        OffsetDateTime paidAt,
        BigDecimal refundedAmount,
        int cancellationCount
) {

    public static SaleRecordResult from(SaleRecord saleRecord) {
        Course course = saleRecord.getCourse();
        Creator creator = course.getCreator();

        return new SaleRecordResult(
                saleRecord.getId(),
                course.getId(),
                course.getTitle(),
                creator.getId(),
                creator.getName(),
                saleRecord.getStudentId(),
                saleRecord.getAmount(),
                saleRecord.getPaidAt(),
                saleRecord.getTotalRefundAmount(),
                saleRecord.getCancellations().size()
        );
    }
}
