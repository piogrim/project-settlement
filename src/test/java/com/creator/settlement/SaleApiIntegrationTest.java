package com.creator.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creator.settlement.settlement.domain.DailySettlement;
import com.creator.settlement.settlement.repository.DailySettlementRepository;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class SaleApiIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @Test
    @DisplayName("이전 월 판매 등록은 409 에러를 반환한다")
    void shouldRejectSaleRegistrationForClosedMonth() throws Exception {
        YearMonth previousMonth = kstClock.currentYearMonth().minusMonths(1);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-closed-month",
                                  "courseId": "course-1",
                                  "studentId": "student-closed",
                                  "amount": 50000,
                                  "paidAt": "%s-15T10:00:00+09:00"
                                }
                                """.formatted(previousMonth)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value(containsString("판매 내역")));
    }

    @Test
    @DisplayName("이전 월 취소 등록은 409 에러를 반환한다")
    void shouldRejectCancellationRegistrationForClosedMonth() throws Exception {
        YearMonth previousMonth = kstClock.currentYearMonth().minusMonths(1);

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-closed-month",
                                  "refundAmount": 10000,
                                  "canceledAt": "%s-15T10:00:00+09:00"
                                }
                                """.formatted(previousMonth)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value(containsString("취소 내역")));
    }

    @Test
    @DisplayName("부분 환불은 판매 목록 조회에서 환불 금액만 반영된다")
    void shouldReflectPartialRefundAmountInSaleList() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleId").value("sale-4"))
                .andExpect(jsonPath("$[0].amount").value(80000))
                .andExpect(jsonPath("$[0].refundedAmount").value(30000))
                .andExpect(jsonPath("$[0].cancellationCount").value(1));
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 400 에러를 반환한다")
    void shouldReturnBadRequestForInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", "2025-03-31")
                        .param("endDate", "2025-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("잘못된 요청"))
                .andExpect(jsonPath("$.message").value(containsString("종료일은 시작일")));
    }

    @Test
    @DisplayName("환불 금액이 원 결제 금액을 초과하면 409 에러를 반환한다")
    void shouldReturnConflictWhenRefundExceedsOriginalAmount() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-over-refund-base",
                                  "courseId": "course-1",
                                  "studentId": "student-over-refund",
                                  "amount": 50000,
                                  "paidAt": "%s-10T10:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-over-refund-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-over-refund",
                                  "refundAmount": 60000,
                                  "canceledAt": "%s-11T10:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value(containsString("원 결제 금액")));
    }

    @Test
    @DisplayName("존재하지 않는 판매 ID로 취소 요청하면 404 에러를 반환한다")
    void shouldReturnNotFoundWhenSaleDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-missing-sale",
                                  "refundAmount": 10000,
                                  "canceledAt": "%s-20T10:00:00+09:00"
                                }
                                """.formatted(kstClock.currentYearMonth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("찾을 수 없음"))
                .andExpect(jsonPath("$.message").value(containsString("sale-missing")));
    }

    @Test
    @DisplayName("동일 월에 여러 취소가 발생하면 환불 금액과 취소 건수가 누적된다")
    void shouldAccumulateMultipleCancellationsWithinSameMonth() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-multi-cancel",
                                  "courseId": "course-1",
                                  "studentId": "student-multi",
                                  "amount": 100000,
                                  "paidAt": "%s-10T09:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-multi-cancel"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-multi-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-multi-1",
                                  "refundAmount": 10000,
                                  "canceledAt": "%s-11T12:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRefundedAmount").value(10000));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-multi-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-multi-2",
                                  "refundAmount": 15000,
                                  "canceledAt": "%s-12T18:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRefundedAmount").value(25000));

        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", currentMonth.atDay(1).toString())
                        .param("endDate", currentMonth.atEndOfMonth().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleId").value("sale-multi-cancel"))
                .andExpect(jsonPath("$[0].refundedAmount").value(25000))
                .andExpect(jsonPath("$[0].cancellationCount").value(2));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(100000))
                .andExpect(jsonPath("$.totalRefundAmount").value(25000))
                .andExpect(jsonPath("$.netSalesAmount").value(75000))
                .andExpect(jsonPath("$.platformFeeAmount").value(15000))
                .andExpect(jsonPath("$.settlementAmount").value(60000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(2));

        DailySettlement saleDaySettlement = dailySettlementRepository
                .findByCreatorIdAndSettlementDate("creator-1", currentMonth.atDay(10))
                .orElseThrow();
        assertThat(saleDaySettlement.getTotalSalesAmount()).isEqualByComparingTo("100000");
        assertThat(saleDaySettlement.getTotalRefundAmount()).isEqualByComparingTo("0");
        assertThat(saleDaySettlement.getSaleCount()).isEqualTo(1);
        assertThat(saleDaySettlement.getCancelCount()).isEqualTo(0);

        DailySettlement firstCancellationSettlement = dailySettlementRepository
                .findByCreatorIdAndSettlementDate("creator-1", currentMonth.atDay(11))
                .orElseThrow();
        assertThat(firstCancellationSettlement.getTotalSalesAmount()).isEqualByComparingTo("0");
        assertThat(firstCancellationSettlement.getTotalRefundAmount()).isEqualByComparingTo("10000");
        assertThat(firstCancellationSettlement.getSaleCount()).isEqualTo(0);
        assertThat(firstCancellationSettlement.getCancelCount()).isEqualTo(1);

        DailySettlement secondCancellationSettlement = dailySettlementRepository
                .findByCreatorIdAndSettlementDate("creator-1", currentMonth.atDay(12))
                .orElseThrow();
        assertThat(secondCancellationSettlement.getTotalSalesAmount()).isEqualByComparingTo("0");
        assertThat(secondCancellationSettlement.getTotalRefundAmount()).isEqualByComparingTo("15000");
        assertThat(secondCancellationSettlement.getSaleCount()).isEqualTo(0);
        assertThat(secondCancellationSettlement.getCancelCount()).isEqualTo(1);
    }
}
