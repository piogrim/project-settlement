package com.creator.settlement.common.time;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class KstPeriodResolver {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public KstRange monthlyRange(YearMonth yearMonth) {
        OffsetDateTime startAt = yearMonth.atDay(1).atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime endExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(KST).toOffsetDateTime();
        return new KstRange(startAt, endExclusive);
    }

    public KstRange dateRange(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startAt = startDate.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime endExclusive = endDate.plusDays(1).atStartOfDay(KST).toOffsetDateTime();
        return new KstRange(startAt, endExclusive);
    }

    public KstRange optionalDateRange(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startAt = startDate == null ? null : startDate.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime endExclusive = endDate == null ? null : endDate.plusDays(1).atStartOfDay(KST).toOffsetDateTime();
        return new KstRange(startAt, endExclusive);
    }

    public record KstRange(OffsetDateTime startAt, OffsetDateTime endExclusive) {
    }
}
