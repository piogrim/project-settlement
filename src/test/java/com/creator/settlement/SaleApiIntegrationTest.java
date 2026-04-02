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
class SaleApiIntegrationTest extends ApiIntegrationTestSupport {

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
                .andExpect(jsonPath("$.message").value("종료일은 시작일과 같거나 이후여야 합니다."));
    }

    @Test
    @DisplayName("환불 금액이 원 결제 금액을 초과하면 409 에러를 반환한다")
    void shouldReturnConflictWhenRefundExceedsOriginalAmount() throws Exception {
        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-over-refund",
                                  "refundAmount": 60000,
                                  "canceledAt": "2025-03-29T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value("환불 금액은 원 결제 금액을 초과할 수 없습니다."));
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
                                  "canceledAt": "2025-03-29T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("판매 내역을 찾을 수 없습니다: sale-missing"));
    }

    @Test
    @DisplayName("동일 월에 여러 취소가 발생하면 환불 금액과 취소 건수가 누적된다")
    void shouldAccumulateMultipleCancellationsWithinSameMonth() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "sale-multi-cancel",
                                  "courseId": "course-1",
                                  "studentId": "student-multi",
                                  "amount": 100000,
                                  "paidAt": "2025-03-29T09:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-multi-cancel"));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-multi-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-multi-1",
                                  "refundAmount": 10000,
                                  "canceledAt": "2025-03-29T12:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRefundedAmount").value(10000));

        mockMvc.perform(post("/api/sales/{saleId}/cancellations", "sale-multi-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancellationId": "cancel-multi-2",
                                  "refundAmount": 15000,
                                  "canceledAt": "2025-03-30T18:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRefundedAmount").value(25000));

        mockMvc.perform(get("/api/creators/{creatorId}/sales", "creator-1")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleId").value("sale-multi-cancel"))
                .andExpect(jsonPath("$[0].refundedAmount").value(25000))
                .andExpect(jsonPath("$[0].cancellationCount").value(2));

        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-03"))
                // 기존 3월 판매 260,000원/취소 110,000원에 테스트 데이터 100,000원 판매와 25,000원 취소를 더한 값이다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalesAmount").value(360000))
                .andExpect(jsonPath("$.totalRefundAmount").value(135000))
                .andExpect(jsonPath("$.netSalesAmount").value(225000))
                .andExpect(jsonPath("$.platformFeeAmount").value(45000))
                .andExpect(jsonPath("$.settlementAmount").value(180000))
                .andExpect(jsonPath("$.saleCount").value(5))
                .andExpect(jsonPath("$.cancelCount").value(4));
    }
}
