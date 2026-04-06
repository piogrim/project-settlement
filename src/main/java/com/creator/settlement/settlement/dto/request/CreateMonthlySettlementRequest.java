package com.creator.settlement.settlement.dto.request;

import com.creator.settlement.settlement.dto.command.CreateMonthlySettlementCommand;
import jakarta.validation.constraints.NotBlank;
import java.time.YearMonth;

public record CreateMonthlySettlementRequest(
        String settlementId,
        @NotBlank(message = "정산 연월은 필수입니다.") String settlementMonth
) {

    public CreateMonthlySettlementCommand toCommand(String creatorId, YearMonth settlementMonth) {
        return new CreateMonthlySettlementCommand(settlementId, creatorId, settlementMonth);
    }
}
