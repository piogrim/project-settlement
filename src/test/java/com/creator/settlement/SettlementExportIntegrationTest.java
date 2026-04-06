package com.creator.settlement;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementExportIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    @DisplayName("운영자 정산 집계를 CSV로 다운로드할 수 있다")
    void shouldExportAdminSettlementSummaryAsCsv() throws Exception {
        mockMvc.perform(get("/api/admin/settlements/export/csv")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"settlement-summary-2025-03-01-to-2025-03-31.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(containsString("정산 시작일,정산 종료일,크리에이터 ID,크리에이터명")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-1,김강사,260000,110000,150000,30000,120000,4,2")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-2,이강사,60000,0,60000,12000,48000,1,0")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-3,박강사,0,0,0,0,0,0,0")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,TOTAL,전체 합계,320000,110000,210000,42000,168000,5,2")));
    }

    @Test
    @DisplayName("운영자 CSV는 원천 판매 테이블을 비워도 사전 집계 데이터로 생성된다")
    void shouldExportAdminSettlementSummaryFromPrecomputedAggregates() throws Exception {
        saleCancellationRepository.deleteAllInBatch();
        saleRecordRepository.deleteAllInBatch();

        mockMvc.perform(get("/api/admin/settlements/export/csv")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-1,김강사,260000,110000,150000,30000,120000,4,2")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,creator-2,이강사,60000,0,60000,12000,48000,1,0")))
                .andExpect(content().string(containsString("2025-03-01,2025-03-31,TOTAL,전체 합계,320000,110000,210000,42000,168000,5,2")));
    }
}
