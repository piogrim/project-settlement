package com.creator.settlement.bootstrap;

import com.creator.settlement.course.domain.Course;
import com.creator.settlement.course.repository.CourseRepository;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.domain.SaleCancellation;
import com.creator.settlement.sale.domain.SaleRecord;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

    private final CreatorRepository creatorRepository;
    private final CourseRepository courseRepository;
    private final SaleRecordRepository saleRecordRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hasSeedData()) {
            return;
        }

        SeedCreators creators = seedCreators();
        SeedCourses courses = seedCourses(creators);
        seedSales(courses);
    }

    private boolean hasSeedData() {
        return creatorRepository.count() > 0 || courseRepository.count() > 0 || saleRecordRepository.count() > 0;
    }

    private SeedCreators seedCreators() {
        Creator creator1 = creatorRepository.save(Creator.builder()
                .id("creator-1")
                .name("김강사")
                .build());
        Creator creator2 = creatorRepository.save(Creator.builder()
                .id("creator-2")
                .name("이강사")
                .build());
        Creator creator3 = creatorRepository.save(Creator.builder()
                .id("creator-3")
                .name("박강사")
                .build());
        return new SeedCreators(creator1, creator2, creator3);
    }

    private SeedCourses seedCourses(SeedCreators creators) {
        Course course1 = courseRepository.save(Course.builder()
                .id("course-1")
                .creator(creators.creator1())
                .title("Spring Boot 입문")
                .build());
        Course course2 = courseRepository.save(Course.builder()
                .id("course-2")
                .creator(creators.creator1())
                .title("JPA 실전")
                .build());
        Course course3 = courseRepository.save(Course.builder()
                .id("course-3")
                .creator(creators.creator2())
                .title("Kotlin 기초")
                .build());
        Course course4 = courseRepository.save(Course.builder()
                .id("course-4")
                .creator(creators.creator3())
                .title("MSA 설계")
                .build());
        return new SeedCourses(course1, course2, course3, course4);
    }

    private void seedSales(SeedCourses courses) {
        saleRecordRepository.save(createSale("sale-1", courses.course1(), "student-1", 50000, "2025-03-05T10:00:00+09:00"));
        saleRecordRepository.save(createSale("sale-2", courses.course1(), "student-2", 50000, "2025-03-15T14:30:00+09:00"));

        SaleRecord sale3 = createSale("sale-3", courses.course2(), "student-3", 80000, "2025-03-20T09:00:00+09:00");
        sale3.addCancellation(createCancellation("cancel-1", 80000, "2025-03-25T12:00:00+09:00"));
        saleRecordRepository.save(sale3);

        SaleRecord sale4 = createSale("sale-4", courses.course2(), "student-4", 80000, "2025-03-22T11:00:00+09:00");
        sale4.addCancellation(createCancellation("cancel-2", 30000, "2025-03-28T18:00:00+09:00"));
        saleRecordRepository.save(sale4);

        SaleRecord sale5 = createSale("sale-5", courses.course3(), "student-5", 60000, "2025-01-31T23:30:00+09:00");
        sale5.addCancellation(createCancellation("cancel-3", 60000, "2025-02-01T09:00:00+09:00"));
        saleRecordRepository.save(sale5);

        saleRecordRepository.save(createSale("sale-6", courses.course3(), "student-6", 60000, "2025-03-10T16:00:00+09:00"));
        saleRecordRepository.save(createSale("sale-7", courses.course4(), "student-7", 120000, "2025-02-14T10:00:00+09:00"));
    }

    private SaleRecord createSale(
            String saleId,
            Course course,
            String studentId,
            long amount,
            String paidAt
    ) {
        return SaleRecord.builder()
                .id(saleId)
                .course(course)
                .studentId(studentId)
                .amount(BigDecimal.valueOf(amount))
                .paidAt(OffsetDateTime.parse(paidAt))
                .build();
    }

    private SaleCancellation createCancellation(String cancellationId, long refundAmount, String canceledAt) {
        return SaleCancellation.builder()
                .id(cancellationId)
                .refundAmount(BigDecimal.valueOf(refundAmount))
                .canceledAt(OffsetDateTime.parse(canceledAt))
                .build();
    }

    private record SeedCreators(Creator creator1, Creator creator2, Creator creator3) {
    }

    private record SeedCourses(Course course1, Course course2, Course course3, Course course4) {
    }
}
