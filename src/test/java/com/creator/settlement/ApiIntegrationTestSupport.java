package com.creator.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.course.domain.Course;
import com.creator.settlement.course.repository.CourseRepository;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.repository.SaleCancellationRepository;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import com.creator.settlement.settlement.repository.DailySettlementRepository;
import com.creator.settlement.settlement.domain.SettlementFeeRate;
import com.creator.settlement.settlement.repository.MonthlySettlementRepository;
import com.creator.settlement.settlement.repository.SettlementFeeRateRepository;
import com.creator.settlement.settlement.service.DailySettlementCommandService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

abstract class ApiIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected KstClock kstClock;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    protected SaleRecordRepository saleRecordRepository;

    @Autowired
    protected SaleCancellationRepository saleCancellationRepository;

    @Autowired
    private MonthlySettlementRepository monthlySettlementRepository;

    @Autowired
    private SettlementFeeRateRepository settlementFeeRateRepository;

    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @Autowired
    private DailySettlementCommandService dailySettlementCommandService;

    @BeforeEach
    void setUpRequiredScenario() {
        clearDatabase();
        seedRequiredScenario();
    }

    private void clearDatabase() {
        monthlySettlementRepository.deleteAllInBatch();
        settlementFeeRateRepository.deleteAllInBatch();
        dailySettlementRepository.deleteAllInBatch();
        saleCancellationRepository.deleteAllInBatch();
        saleRecordRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        creatorRepository.deleteAllInBatch();
    }

    private void seedRequiredScenario() {
        Creator creator1 = saveCreator("creator-1", "김강사");
        Creator creator2 = saveCreator("creator-2", "이강사");
        Creator creator3 = saveCreator("creator-3", "박강사");

        Course course1 = saveCourse("course-1", creator1, "Spring Boot 입문");
        Course course2 = saveCourse("course-2", creator1, "JPA 실전");
        Course course3 = saveCourse("course-3", creator2, "Kotlin 기초");
        Course course4 = saveCourse("course-4", creator3, "MSA 설계");

        saveSale("sale-1", course1, "student-1", 50000, "2025-03-05T10:00:00+09:00");
        saveSale("sale-2", course1, "student-2", 50000, "2025-03-15T14:30:00+09:00");

        SaleRecord sale3 = sale("sale-3", course2, "student-3", 80000, "2025-03-20T09:00:00+09:00");
        sale3.addCancellation(cancellation("cancel-1", 80000, "2025-03-25T12:00:00+09:00"));
        saveSaleRecord(sale3);

        SaleRecord sale4 = sale("sale-4", course2, "student-4", 80000, "2025-03-22T11:00:00+09:00");
        sale4.addCancellation(cancellation("cancel-2", 30000, "2025-03-28T18:00:00+09:00"));
        saveSaleRecord(sale4);

        SaleRecord sale5 = sale("sale-5", course3, "student-5", 60000, "2025-01-31T23:30:00+09:00");
        sale5.addCancellation(cancellation("cancel-3", 60000, "2025-02-01T09:00:00+09:00"));
        saveSaleRecord(sale5);

        saveSale("sale-6", course3, "student-6", 60000, "2025-03-10T16:00:00+09:00");
        saveSale("sale-7", course4, "student-7", 120000, "2025-02-14T10:00:00+09:00");
    }

    private Creator saveCreator(String creatorId, String name) {
        return creatorRepository.save(Creator.builder()
                .id(creatorId)
                .name(name)
                .build());
    }

    private Course saveCourse(String courseId, Creator creator, String title) {
        return courseRepository.save(Course.builder()
                .id(courseId)
                .creator(creator)
                .title(title)
                .build());
    }

    private void saveSale(String saleId, Course course, String studentId, long amount, String paidAt) {
        saveSaleRecord(sale(saleId, course, studentId, amount, paidAt));
    }

    private void saveSaleRecord(SaleRecord saleRecord) {
        saleRecordRepository.save(saleRecord);
        dailySettlementCommandService.addSale(
                saleRecord.getCourse().getCreator().getId(),
                saleRecord.getPaidAt(),
                saleRecord.getAmount()
        );

        for (SaleCancellation cancellation : saleRecord.getCancellations()) {
            dailySettlementCommandService.addCancellation(
                    saleRecord.getCourse().getCreator().getId(),
                    cancellation.getCanceledAt(),
                    cancellation.getRefundAmount()
            );
        }
    }

    private SaleRecord sale(String saleId, Course course, String studentId, long amount, String paidAt) {
        return SaleRecord.builder()
                .id(saleId)
                .course(course)
                .studentId(studentId)
                .amount(BigDecimal.valueOf(amount))
                .paidAt(OffsetDateTime.parse(paidAt))
                .build();
    }

    private SaleCancellation cancellation(String cancellationId, long refundAmount, String canceledAt) {
        return SaleCancellation.builder()
                .id(cancellationId)
                .refundAmount(BigDecimal.valueOf(refundAmount))
                .canceledAt(OffsetDateTime.parse(canceledAt))
                .build();
    }

    protected ResultActions createCreatorSettlement(String creatorId, String settlementId, String settlementMonth)
            throws Exception {
        return mockMvc.perform(post("/api/creators/{creatorId}/settlements", creatorId)
                .contentType("application/json")
                .content("""
                        {
                          "settlementId": "%s",
                          "settlementMonth": "%s"
                        }
                        """.formatted(settlementId, settlementMonth)));
    }

    protected SettlementFeeRate saveFeeRateHistory(String settlementFeeRateId, String effectiveFrom, String feeRate) {
        return settlementFeeRateRepository.save(SettlementFeeRate.builder()
                .id(settlementFeeRateId)
                .effectiveFrom(YearMonth.parse(effectiveFrom))
                .feeRate(new BigDecimal(feeRate))
                .createdAt(kstClock.now())
                .build());
    }
}
