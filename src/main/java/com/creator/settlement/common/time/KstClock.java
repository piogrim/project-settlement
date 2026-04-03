package com.creator.settlement.common.time;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class KstClock {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public ZoneId zoneId() {
        return KST;
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now(KST);
    }

    public YearMonth currentYearMonth() {
        return YearMonth.now(KST);
    }
}
