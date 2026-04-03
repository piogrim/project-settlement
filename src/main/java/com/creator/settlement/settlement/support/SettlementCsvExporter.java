package com.creator.settlement.settlement.support;

import com.creator.settlement.settlement.dto.AdminSettlementSummaryResult;
import com.creator.settlement.settlement.dto.CreatorSettlementSummaryItem;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class SettlementCsvExporter {

    public String export(AdminSettlementSummaryResult summary) {
        StringBuilder csv = new StringBuilder();
        csv.append(header()).append('\n');

        for (CreatorSettlementSummaryItem item : summary.items()) {
            csv.append(toRow(summary, item)).append('\n');
        }

        csv.append(totalRow(summary));
        return csv.toString();
    }

    private String header() {
        return String.join(",",
                "정산 시작일",
                "정산 종료일",
                "크리에이터 ID",
                "크리에이터명",
                "총 판매 금액",
                "총 환불 금액",
                "순 판매 금액",
                "플랫폼 수수료",
                "정산 예정 금액",
                "판매 건수",
                "취소 건수"
        );
    }

    private String toRow(AdminSettlementSummaryResult summary, CreatorSettlementSummaryItem item) {
        return join(
                summary.startDate(),
                summary.endDate(),
                item.creatorId(),
                item.creatorName(),
                item.totalSalesAmount(),
                item.totalRefundAmount(),
                item.netSalesAmount(),
                item.platformFeeAmount(),
                item.settlementAmount(),
                item.saleCount(),
                item.cancelCount()
        );
    }

    private String totalRow(AdminSettlementSummaryResult summary) {
        return join(
                summary.startDate(),
                summary.endDate(),
                "TOTAL",
                "전체 합계",
                "",
                "",
                "",
                "",
                summary.totalSettlementAmount(),
                "",
                ""
        );
    }

    private String join(Object... values) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object value : values) {
            joiner.add(escape(value));
        }
        return joiner.toString();
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
