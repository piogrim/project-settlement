package com.creator.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementBoundaryIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    @DisplayName("월 경계 취소는 판매 월과 취소 월에 분리 반영된다")
    void shouldSplitBoundaryCancellationAcrossDifferentMonths() throws Exception {
        createCreatorSettlement("creator-2", "settlement-creator-2-2025-01", "2025-01")
                .andExpect(status().isCreated());
        createCreatorSettlement("creator-2", "settlement-creator-2-2025-02", "2025-02")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-2")
                        .param("yearMonth", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(60000))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(48000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(0));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-2")
                        .param("yearMonth", "2025-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(0))
                .andExpect(jsonPath("$.totalRefundAmount").value(60000))
                .andExpect(jsonPath("$.platformFeeAmount").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(-60000))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancelCount").value(1));

        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-2")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleId").value("sale-5"))
                .andExpect(jsonPath("$[0].paidAt").value("2025-01-31T23:30:00+09:00"))
                .andExpect(jsonPath("$[0].refundedAmount").value(60000))
                .andExpect(jsonPath("$[0].cancellationCount").value(1));
    }

    @Test
    @DisplayName("월 시작 시각의 판매는 포함되고 다음 달 시작 시각의 판매는 제외된다")
    void shouldIncludeMonthStartSaleAndExcludeNextMonthStartSale() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();
        YearMonth nextMonth = currentMonth.plusMonths(1);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-current-start",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-1",
                                  "amount": 10000,
                                  "paidAt": "%s-01T00:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-current-start"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-next-start",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-2",
                                  "amount": 20000,
                                  "paidAt": "%s-01T00:00:00+09:00"
                                }
                                """.formatted(nextMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-next-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(10000))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(10000))
                .andExpect(jsonPath("$.platformFeeAmount").value(2000))
                .andExpect(jsonPath("$.settlementAmount").value(8000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(0));

        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", currentMonth.atDay(1).toString())
                        .param("endDate", currentMonth.atEndOfMonth().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].saleId").value("sale-boundary-current-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", nextMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(20000))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(20000))
                .andExpect(jsonPath("$.platformFeeAmount").value(4000))
                .andExpect(jsonPath("$.settlementAmount").value(16000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(0));
    }

    @Test
    @DisplayName("월 시작 시각의 취소는 포함되고 다음 달 시작 시각의 취소는 다음 달로 넘어간다")
    void shouldIncludeMonthStartCancellationAndDeferNextMonthStartCancellation() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();
        YearMonth nextMonth = currentMonth.plusMonths(1);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-cancel-base",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-cancel",
                                  "amount": 40000,
                                  "paidAt": "%s-01T09:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-cancel-base"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-boundary-cancel-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-boundary-march-start",
                                  "refundAmount": 10000,
                                  "canceledAt": "%s-01T00:00:00+09:00"
                                }
                                """.formatted(currentMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cancellationId").value("cancel-boundary-march-start"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-boundary-cancel-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-boundary-april-start",
                                  "refundAmount": 5000,
                                  "canceledAt": "%s-01T00:00:00+09:00"
                                }
                                """.formatted(nextMonth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cancellationId").value("cancel-boundary-april-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(40000))
                .andExpect(jsonPath("$.totalRefundAmount").value(10000))
                .andExpect(jsonPath("$.netSalesAmount").value(30000))
                .andExpect(jsonPath("$.platformFeeAmount").value(6000))
                .andExpect(jsonPath("$.settlementAmount").value(24000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(1));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", nextMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(0))
                .andExpect(jsonPath("$.totalRefundAmount").value(5000))
                .andExpect(jsonPath("$.netSalesAmount").value(-5000))
                .andExpect(jsonPath("$.platformFeeAmount").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(-5000))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancelCount").value(1));
    }

    @Test
    @DisplayName("다른 오프셋으로 들어온 결제 시각도 KST 기준 월 경계로 해석된다")
    void shouldResolveMonthlyBoundaryUsingKstForDifferentOffsets() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();
        String currentMonthStartInUtc = currentMonth.atDay(1).minusDays(1) + "T15:00:00Z";
        String nextMonthStartInUtc = currentMonth.atEndOfMonth() + "T15:00:00Z";

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-offset-current-z",
                                  "courseId": "course-1",
                                  "studentId": "student-offset-z",
                                  "amount": 7000,
                                  "paidAt": "%s"
                                }
                                """.formatted(currentMonthStartInUtc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-offset-current-z"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-offset-next",
                                  "courseId": "course-1",
                                  "studentId": "student-offset-2",
                                  "amount": 9000,
                                  "paidAt": "%s"
                                }
                                """.formatted(nextMonthStartInUtc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-offset-next"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(7000))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(7000))
                .andExpect(jsonPath("$.platformFeeAmount").value(1400))
                .andExpect(jsonPath("$.settlementAmount").value(5600))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(0));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", currentMonth.plusMonths(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(9000))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(9000))
                .andExpect(jsonPath("$.platformFeeAmount").value(1800))
                .andExpect(jsonPath("$.settlementAmount").value(7200))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancelCount").value(0));
    }
}
