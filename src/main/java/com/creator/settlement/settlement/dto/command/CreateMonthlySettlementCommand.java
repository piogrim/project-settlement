package com.creator.settlement.settlement.dto.command;

import java.time.YearMonth;

public record CreateMonthlySettlementCommand(
        String settlementId,
        String creatorId,
        YearMonth settlementMonth
) {
}
