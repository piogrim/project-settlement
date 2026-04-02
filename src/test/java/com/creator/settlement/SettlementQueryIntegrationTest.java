package com.creator.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementQueryIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    @DisplayName("creator-1의 2025-03 정산은 샘플 시나리오 기대값과 일치한다")
    void shouldReturnExpectedMonthlySettlementForCreator1InMarch() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorId").value("creator-1"))
                .andExpect(jsonPath("$.settlementMonth").value("2025-03"))
                .andExpect(jsonPath("$.totalSalesAmount").value(260000))
                .andExpect(jsonPath("$.totalRefundAmount").value(110000))
                .andExpect(jsonPath("$.netSalesAmount").value(150000))
                .andExpect(jsonPath("$.platformFeeAmount").value(30000))
                .andExpect(jsonPath("$.settlementAmount").value(120000))
                .andExpect(jsonPath("$.saleCount").value(4))
                .andExpect(jsonPath("$.cancelCount").value(2));
    }

    @Test
    @DisplayName("빈 월 조회는 모든 금액과 건수를 0으로 반환한다")
    void shouldReturnZeroAmountsForEmptyMonth() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-3")
                        .param("yearMonth", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorId").value("creator-3"))
                .andExpect(jsonPath("$.totalSalesAmount").value(0))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(0))
                .andExpect(jsonPath("$.platformFeeAmount").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(0))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancelCount").value(0));
    }

    @Test
    @DisplayName("미래 월 정산 조회는 모든 금액과 건수를 0으로 반환한다")
    void shouldReturnZeroAmountsForFutureMonth() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2026-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorId").value("creator-1"))
                .andExpect(jsonPath("$.settlementMonth").value("2026-12"))
                .andExpect(jsonPath("$.totalSalesAmount").value(0))
                .andExpect(jsonPath("$.totalRefundAmount").value(0))
                .andExpect(jsonPath("$.netSalesAmount").value(0))
                .andExpect(jsonPath("$.platformFeeAmount").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(0))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancelCount").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 크리에이터의 정산 조회는 404 에러를 반환한다")
    void shouldReturnNotFoundForMissingCreator() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-missing")
                        .param("yearMonth", "2025-03"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("크리에이터를 찾을 수 없습니다: creator-missing"));
    }

    @Test
    @DisplayName("잘못된 yearMonth 형식은 400 에러를 반환한다")
    void shouldReturnBadRequestForInvalidYearMonthFormat() throws Exception {
        mockMvc.perform(get("/api/creators/{creatorId}/settlements/monthly", "creator-1")
                        .param("yearMonth", "2025/03"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("잘못된 요청"))
                .andExpect(jsonPath("$.message").value("yearMonth는 yyyy-MM 형식이어야 합니다."));
    }
}
