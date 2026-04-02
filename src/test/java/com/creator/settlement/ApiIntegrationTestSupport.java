package com.creator.settlement;

import com.creator.settlement.course.domain.Course;
import com.creator.settlement.course.repository.CourseRepository;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.repository.SaleCancellationRepository;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

abstract class ApiIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SaleRecordRepository saleRecordRepository;

    @Autowired
    private SaleCancellationRepository saleCancellationRepository;

    @BeforeEach
    void setUpRequiredScenario() {
        clearDatabase();
        seedRequiredScenario();
    }

    private void clearDatabase() {
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
        saleRecordRepository.save(sale3);

        SaleRecord sale4 = sale("sale-4", course2, "student-4", 80000, "2025-03-22T11:00:00+09:00");
        sale4.addCancellation(cancellation("cancel-2", 30000, "2025-03-28T18:00:00+09:00"));
        saleRecordRepository.save(sale4);

        SaleRecord sale5 = sale("sale-5", course3, "student-5", 60000, "2025-01-31T23:30:00+09:00");
        sale5.addCancellation(cancellation("cancel-3", 60000, "2025-02-01T09:00:00+09:00"));
        saleRecordRepository.save(sale5);

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
        saleRecordRepository.save(sale(saleId, course, studentId, amount, paidAt));
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
}
