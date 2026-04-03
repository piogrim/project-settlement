package com.creator.settlement.settlement.dto;

import java.time.YearMonth;

public record RegisterSettlementCommand(
        String settlementId,
        String creatorId,
        YearMonth settlementMonth
) {
}
