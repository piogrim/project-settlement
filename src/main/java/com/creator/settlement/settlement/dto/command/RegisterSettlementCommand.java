package com.creator.settlement.settlement.dto.command;

import java.time.YearMonth;

public record RegisterSettlementCommand(
        String settlementId,
        String creatorId,
        YearMonth settlementMonth
) {
}
