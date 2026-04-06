package com.creator.settlement;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementFeeRateHistoryIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    @DisplayName("이전 월 정산 생성 시 해당 월에 적용되는 수수료율 이력이 스냅샷에 반영된다")
    void shouldApplyHistoricalFeeRateWhenCreatingSettlementSnapshot() throws Exception {
        saveFeeRateHistory("fee-rate-2025-03", "2025-03", "0.1500");

        createCreatorSettlement("creator-1", "settlement-creator-1-2025-03", "2025-03")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.feeRate").value(0.1500))
                .andExpect(jsonPath("$.platformFeeAmount").value(22500))
                .andExpect(jsonPath("$.settlementAmount").value(127500));
    }

    @Test
    @DisplayName("운영자 기간 집계도 각 월의 수수료율 이력을 적용해 계산한다")
    void shouldApplyHistoricalFeeRateToAdminSummary() throws Exception {
        saveFeeRateHistory("fee-rate-2025-03", "2025-03", "0.1500");

        mockMvc.perform(get("/api/admin/settlements")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].creatorId").value("creator-1"))
                .andExpect(jsonPath("$.items[0].platformFeeAmount").value(22500))
                .andExpect(jsonPath("$.items[0].settlementAmount").value(127500))
                .andExpect(jsonPath("$.items[1].creatorId").value("creator-2"))
                .andExpect(jsonPath("$.items[1].platformFeeAmount").value(9000))
                .andExpect(jsonPath("$.items[1].settlementAmount").value(51000))
                .andExpect(jsonPath("$.totalSettlementAmount").value(178500));
    }

    @Test
    @DisplayName("여러 달을 포함한 기간 집계는 월별 수수료율을 각각 적용해 합산한다")
    void shouldApplyFeeRatePerMonthAcrossMultiMonthRange() throws Exception {
        saveFeeRateHistory("fee-rate-2025-03", "2025-03", "0.1500");

        mockMvc.perform(get("/api/admin/settlements")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].creatorId").value("creator-1"))
                .andExpect(jsonPath("$.items[0].platformFeeAmount").value(22500))
                .andExpect(jsonPath("$.items[0].settlementAmount").value(127500))
                .andExpect(jsonPath("$.items[1].creatorId").value("creator-2"))
                .andExpect(jsonPath("$.items[1].totalSalesAmount").value(120000))
                .andExpect(jsonPath("$.items[1].totalRefundAmount").value(60000))
                .andExpect(jsonPath("$.items[1].platformFeeAmount").value(21000))
                .andExpect(jsonPath("$.items[1].settlementAmount").value(39000))
                .andExpect(jsonPath("$.items[1].saleCount").value(2))
                .andExpect(jsonPath("$.items[1].cancelCount").value(1))
                .andExpect(jsonPath("$.items[2].creatorId").value("creator-3"))
                .andExpect(jsonPath("$.items[2].platformFeeAmount").value(24000))
                .andExpect(jsonPath("$.items[2].settlementAmount").value(96000))
                .andExpect(jsonPath("$.totalSettlementAmount").value(262500));
    }

    @Test
    @DisplayName("운영자 CSV 응답도 각 월의 수수료율 이력을 반영한 집계 결과를 내려준다")
    void shouldApplyHistoricalFeeRateToAdminCsvExport() throws Exception {
        saveFeeRateHistory("fee-rate-2025-03", "2025-03", "0.1500");

        mockMvc.perform(get("/api/admin/settlements/export/csv")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-1,김강사,260000,110000,150000,22500,127500,4,2")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-2,이강사,60000,0,60000,9000,51000,1,0")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,TOTAL,전체 합계,320000,110000,210000,31500,178500,5,2")));
    }

    @Test
    @DisplayName("수수료율 이력은 적용 시작 연월 기준 내림차순으로 조회할 수 있다")
    void shouldListSettlementFeeRatesInDescendingEffectiveFromOrder() throws Exception {
        YearMonth firstFutureMonth = kstClock.currentYearMonth().plusMonths(1);
        YearMonth secondFutureMonth = kstClock.currentYearMonth().plusMonths(2);

        registerFeeRate("fee-rate-" + firstFutureMonth, firstFutureMonth.toString(), "0.1500")
                .andExpect(status().isCreated());
        registerFeeRate("fee-rate-" + secondFutureMonth, secondFutureMonth.toString(), "0.1000")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/settlement-fee-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].effectiveFrom").value(secondFutureMonth.toString()))
                .andExpect(jsonPath("$[0].feeRate").value(0.1000))
                .andExpect(jsonPath("$[1].effectiveFrom").value(firstFutureMonth.toString()))
                .andExpect(jsonPath("$[1].feeRate").value(0.1500));
    }

    @Test
    @DisplayName("과거 월에 적용되는 수수료율 이력은 생성할 수 없다")
    void shouldRejectCreatingPastFeeRate() throws Exception {
        YearMonth pastMonth = kstClock.currentYearMonth().minusMonths(1);

        registerFeeRate("fee-rate-past", pastMonth.toString(), "0.1800")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value(containsString("과거 월")));
    }

    @Test
    @DisplayName("현재 월과 미래 월에 적용될 수수료율 이력은 금액을 수정할 수 있다")
    void shouldUpdateCurrentOrFutureFeeRate() throws Exception {
        YearMonth currentMonth = kstClock.currentYearMonth();
        YearMonth futureMonth = kstClock.currentYearMonth().plusMonths(1);

        registerFeeRate("fee-rate-current", currentMonth.toString(), "0.1800")
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/admin/settlement-fee-rates/{settlementFeeRateId}", "fee-rate-current")
                        .contentType("application/json")
                        .content("""
                                {
                                  "feeRate": 0.1600
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementFeeRateId").value("fee-rate-current"))
                .andExpect(jsonPath("$.effectiveFrom").value(currentMonth.toString()))
                .andExpect(jsonPath("$.feeRate").value(0.1600));

        registerFeeRate("fee-rate-future", futureMonth.toString(), "0.1800")
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/admin/settlement-fee-rates/{settlementFeeRateId}", "fee-rate-future")
                        .contentType("application/json")
                        .content("""
                                {
                                  "feeRate": 0.1200
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementFeeRateId").value("fee-rate-future"))
                .andExpect(jsonPath("$.effectiveFrom").value(futureMonth.toString()))
                .andExpect(jsonPath("$.feeRate").value(0.1200));
    }

    @Test
    @DisplayName("과거 월에 적용되는 수수료율 이력은 수정할 수 없다")
    void shouldRejectUpdatingPastFeeRate() throws Exception {
        YearMonth pastMonth = kstClock.currentYearMonth().minusMonths(1);

        saveFeeRateHistory("fee-rate-past", pastMonth.toString(), "0.1800");

        mockMvc.perform(put("/api/admin/settlement-fee-rates/{settlementFeeRateId}", "fee-rate-past")
                        .contentType("application/json")
                        .content("""
                                {
                                  "feeRate": 0.1200
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("충돌"))
                .andExpect(jsonPath("$.message").value(containsString("과거 월")));
    }

    private org.springframework.test.web.servlet.ResultActions registerFeeRate(
            String settlementFeeRateId,
            String effectiveFrom,
            String feeRate
    ) throws Exception {
        return mockMvc.perform(post("/api/admin/settlement-fee-rates")
                .contentType("application/json")
                .content("""
                        {
                          "settlementFeeRateId": "%s",
                          "effectiveFrom": "%s",
                          "feeRate": %s
                        }
                        """.formatted(settlementFeeRateId, effectiveFrom, feeRate)));
    }
}
