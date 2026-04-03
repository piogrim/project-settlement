package com.creator.settlement.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.YearMonth;

public record CreateSettlementRequest(
        String settlementId,
        @NotBlank(message = "정산 연월은 필수입니다.") String settlementMonth
) {

    public RegisterSettlementCommand toCommand(String creatorId, YearMonth settlementMonth) {
        return new RegisterSettlementCommand(settlementId, creatorId, settlementMonth);
    }
}
