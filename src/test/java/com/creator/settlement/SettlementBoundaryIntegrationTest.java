package com.creator.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-march-start",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-1",
                                  "amount": 10000,
                                  "paidAt": "2025-03-01T00:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-march-start"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-april-start",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-2",
                                  "amount": 20000,
                                  "paidAt": "2025-04-01T00:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-april-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-03"))
                // 기존 3월 판매 260,000원에 월 시작 판매 10,000원을 더하고, 4월 시작 판매 20,000원은 제외한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(270000))
                .andExpect(jsonPath("$.totalRefundAmount").value(110000))
                .andExpect(jsonPath("$.netSalesAmount").value(160000))
                .andExpect(jsonPath("$.platformFeeAmount").value(32000))
                .andExpect(jsonPath("$.settlementAmount").value(128000))
                .andExpect(jsonPath("$.saleCount").value(5))
                .andExpect(jsonPath("$.cancelCount").value(2));

        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[4].saleId").value("sale-boundary-march-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-04"))
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
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-boundary-cancel-base",
                                  "courseId": "course-1",
                                  "studentId": "student-boundary-cancel",
                                  "amount": 40000,
                                  "paidAt": "2025-02-28T23:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-boundary-cancel-base"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-boundary-cancel-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-boundary-march-start",
                                  "refundAmount": 10000,
                                  "canceledAt": "2025-03-01T00:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cancellationId").value("cancel-boundary-march-start"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-boundary-cancel-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-boundary-april-start",
                                  "refundAmount": 5000,
                                  "canceledAt": "2025-04-01T00:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cancellationId").value("cancel-boundary-april-start"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-03"))
                // 기존 3월 취소 110,000원에 3월 1일 00:00:00 취소 10,000원을 더하고, 4월 1일 취소 5,000원은 제외한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(260000))
                .andExpect(jsonPath("$.totalRefundAmount").value(120000))
                .andExpect(jsonPath("$.netSalesAmount").value(140000))
                .andExpect(jsonPath("$.platformFeeAmount").value(28000))
                .andExpect(jsonPath("$.settlementAmount").value(112000))
                .andExpect(jsonPath("$.saleCount").value(4))
                .andExpect(jsonPath("$.cancelCount").value(3));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-04"))
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
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-offset-march",
                                  "courseId": "course-1",
                                  "studentId": "student-offset-1",
                                  "amount": 7000,
                                  "paidAt": "2025-02-28T15:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-offset-march"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-offset-april",
                                  "courseId": "course-1",
                                  "studentId": "student-offset-2",
                                  "amount": 9000,
                                  "paidAt": "2025-03-31T15:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-offset-april"));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-03"))
                // UTC 입력 2025-02-28T15:00:00Z는 KST로 2025-03-01T00:00:00+09:00이므로 3월 매출에 포함된다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(267000))
                .andExpect(jsonPath("$.totalRefundAmount").value(110000))
                .andExpect(jsonPath("$.netSalesAmount").value(157000))
                .andExpect(jsonPath("$.platformFeeAmount").value(31400))
                .andExpect(jsonPath("$.settlementAmount").value(125600))
                .andExpect(jsonPath("$.saleCount").value(5))
                .andExpect(jsonPath("$.cancelCount").value(2));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-04"))
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
