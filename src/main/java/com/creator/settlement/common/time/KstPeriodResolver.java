package com.creator.settlement.common.time;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KstPeriodResolver {

    private final KstClock kstClock;

    public KstRange monthlyRange(YearMonth yearMonth) {
        OffsetDateTime startAt = yearMonth.atDay(1).atStartOfDay(kstClock.zoneId()).toOffsetDateTime();
        OffsetDateTime endExclusive = yearMonth.plusMonths(1)
                .atDay(1)
                .atStartOfDay(kstClock.zoneId())
                .toOffsetDateTime();
        return new KstRange(startAt, endExclusive);
    }

    public KstRange optionalDateRange(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startAt = startDate == null
                ? null
                : startDate.atStartOfDay(kstClock.zoneId()).toOffsetDateTime();
        OffsetDateTime endExclusive = endDate == null
                ? null
                : endDate.plusDays(1).atStartOfDay(kstClock.zoneId()).toOffsetDateTime();
        return new KstRange(startAt, endExclusive);
    }

    public record KstRange(OffsetDateTime startAt, OffsetDateTime endExclusive) {
    }
}
