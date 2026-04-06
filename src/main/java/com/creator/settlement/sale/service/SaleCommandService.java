package com.creator.settlement.sale.service;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.course.domain.Course;
import com.creator.settlement.course.repository.CourseRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.dto.RegisterSaleCancellationCommand;
import com.creator.settlement.sale.dto.RegisterSaleCommand;
import com.creator.settlement.sale.dto.SaleCancellationResult;
import com.creator.settlement.sale.dto.SaleRecordResult;
import com.creator.settlement.settlement.service.DailySettlementAggregateService;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class SaleCommandService {

    private final CourseRepository courseRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final DailySettlementAggregateService dailySettlementAggregateService;
    private final KstClock kstClock;

    public SaleRecordResult registerSale(RegisterSaleCommand command) {
        validateWritableMonth(command.paidAt(), "마감된 월의 판매 내역은 등록할 수 없습니다.");

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("강의를 찾을 수 없습니다: " + command.courseId()));

        SaleRecord saleRecord = SaleRecord.builder()
                .id(resolveId(command.saleId(), "sale"))
                .course(course)
                .studentId(command.studentId())
                .amount(command.amount())
                .paidAt(command.paidAt())
                .build();

        SaleRecord savedSaleRecord = saleRecordRepository.save(saleRecord);
        dailySettlementAggregateService.addSale(
                course.getCreator().getId(),
                savedSaleRecord.getPaidAt(),
                savedSaleRecord.getAmount()
        );

        return SaleRecordResult.from(savedSaleRecord);
    }

    public SaleCancellationResult registerCancellation(RegisterSaleCancellationCommand command) {
        validateWritableMonth(command.canceledAt(), "마감된 월의 취소 내역은 등록할 수 없습니다.");

        SaleRecord saleRecord = saleRecordRepository.findByIdForUpdate(command.saleId())
                .orElseThrow(() -> new ResourceNotFoundException("판매 내역을 찾을 수 없습니다: " + command.saleId()));

        BigDecimal nextTotalRefundAmount = saleRecord.getTotalRefundAmount().add(command.refundAmount());
        if (nextTotalRefundAmount.compareTo(saleRecord.getAmount()) > 0) {
            throw new BusinessRuleViolationException("환불 금액은 원 결제 금액을 초과할 수 없습니다.");
        }

        SaleCancellation cancellation = SaleCancellation.builder()
                .id(resolveId(command.cancellationId(), "cancel"))
                .refundAmount(command.refundAmount())
                .canceledAt(command.canceledAt())
                .build();

        saleRecord.addCancellation(cancellation);
        saleRecordRepository.save(saleRecord);
        dailySettlementAggregateService.addCancellation(
                saleRecord.getCourse().getCreator().getId(),
                cancellation.getCanceledAt(),
                cancellation.getRefundAmount()
        );

        return SaleCancellationResult.from(cancellation, saleRecord.getTotalRefundAmount());
    }

    private String resolveId(String candidate, String prefix) {
        return candidate == null || candidate.isBlank() ? prefix + "-" + UUID.randomUUID() : candidate;
    }

    private void validateWritableMonth(OffsetDateTime occurredAt, String message) {
        YearMonth occurredMonth = YearMonth.from(occurredAt.atZoneSameInstant(kstClock.zoneId()));
        if (occurredMonth.isBefore(kstClock.currentYearMonth())) {
            throw new BusinessRuleViolationException(message);
        }
    }
}
