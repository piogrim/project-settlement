package com.creator.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementStatusIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    @DisplayName("정산 생성 후 PENDING에서 CONFIRMED를 거쳐 PAID로 상태를 전이할 수 있다")
    void shouldTransitionSettlementStatusFromPendingToConfirmedToPaid() throws Exception {
        createSettlement("creator-1", "settlement-creator-1-2025-03", "2025-03")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settlementId").value("settlement-creator-1-2025-03"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalSalesAmount").value(260000))
                .andExpect(jsonPath("$.totalRefundAmount").value(110000))
                .andExpect(jsonPath("$.settlementAmount").value(120000))
                .andExpect(jsonPath("$.confirmedAt").doesNotExist())
                .andExpect(jsonPath("$.paidAt").doesNotExist());

        mockMvc.perform(post("/api/admin/settlements/{settlementId}/confirm", "settlement-creator-1-2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedAt").isNotEmpty())
                .andExpect(jsonPath("$.paidAt").doesNotExist());

        mockMvc.perform(post("/api/admin/settlements/{settlementId}/pay", "settlement-creator-1-2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.confirmedAt").isNotEmpty())
                .andExpect(jsonPath("$.paidAt").isNotEmpty());
    }

    @Test
    @DisplayName("같은 크리에이터와 정산 월로 정산을 두 번 생성할 수 없다")
    void shouldPreventDuplicateSettlementForSameCreatorAndMonth() throws Exception {
        createSettlement("creator-1", "settlement-creator-1-2025-03", "2025-03");

        createSettlement("creator-1", "settlement-duplicate", "2025-03")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value("해당 크리에이터의 정산이 이미 존재합니다: creator-1 / 2025-03"));
    }

    @Test
    @DisplayName("CONFIRMED 이전에는 정산을 PAID로 전이할 수 없다")
    void shouldRejectPayBeforeConfirm() throws Exception {
        createSettlement("creator-1", "settlement-pay-before-confirm", "2025-03");

        mockMvc.perform(post("/api/admin/settlements/{settlementId}/pay", "settlement-pay-before-confirm"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value("CONFIRMED 상태의 정산만 지급 완료로 변경할 수 있습니다."));
    }

    @Test
    @DisplayName("현재 월은 정산 생성 대상이 아니고 조회로만 확인할 수 있다")
    void shouldRejectSettlementCreationForCurrentMonth() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();

        createSettlement("creator-1", "settlement-current-month", currentMonth.toString())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value("정산 생성은 이전 월에 대해서만 가능합니다."));
    }

    @Test
    @DisplayName("미래 월은 정산 생성 대상이 아니고 조회로만 확인할 수 있다")
    void shouldRejectSettlementCreationForFutureMonth() throws Exception {
        YearMonth futureMonth = kstClock.currentYearMonth().plusMonths(1);

        createSettlement("creator-1", "settlement-future-month", futureMonth.toString())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value("정산 생성은 이전 월에 대해서만 가능합니다."));
    }

    @Test
    @DisplayName("정산 생성 요청에 settlementMonth가 없으면 400 에러를 반환한다")
    void shouldReturnBadRequestWhenSettlementMonthIsMissing() throws Exception {
        mockMvc.perform(post("/api/creators/{creatorId}/settlements", "creator-1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "settlementId": "settlement-missing-month"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("잘못된 요청"))
                .andExpect(jsonPath("$.message").value("정산 연월은 필수입니다."));
    }

    private ResultActions createSettlement(String creatorId, String settlementId, String settlementMonth)
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
}
