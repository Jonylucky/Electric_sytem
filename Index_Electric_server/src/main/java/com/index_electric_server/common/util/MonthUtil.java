package com.index_electric_server.common.util;

import com.index_electric_server.common.exception.BadRequestException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class MonthUtil {
    private MonthUtil() {}

    // YYYY-MM
    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public static String requireValidMonth(String month) {
        if (month == null || month.isBlank()) {
            throw new BadRequestException("month is required (YYYY-MM)");
        }
        String m = month.trim();
        if (!MONTH_PATTERN.matcher(m).matches()) {
            throw new BadRequestException("Invalid month format. Expect YYYY-MM");
        }
        // extra parse safety
        try {
            YearMonth.parse(m, MONTH_FMT);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid month value");
        }
        return m;
    }

    /**
     * Rule: nếu day < cutoffDay => thuộc tháng trước, ngược lại thuộc tháng hiện tại.
     * cutoffDay thường 23.
     */
    public static String monthFromInstant(Instant instant, ZoneId zone, int cutoffDay) {
        if (instant == null) throw new BadRequestException("timestamp/captured_at is required to derive month");
        if (cutoffDay < 1 || cutoffDay > 31) throw new BadRequestException("cutoffDay must be 1..31");

        LocalDateTime ldt = LocalDateTime.ofInstant(instant, zone);
        LocalDate ref = (ldt.getDayOfMonth() < cutoffDay)
                ? ldt.toLocalDate().minusMonths(1).withDayOfMonth(1)
                : ldt.toLocalDate().withDayOfMonth(1);

        return YearMonth.from(ref).format(MONTH_FMT);
    }

    public static String previousMonth(String month) {
        String m = requireValidMonth(month);
        YearMonth ym = YearMonth.parse(m, MONTH_FMT).minusMonths(1);
        return ym.format(MONTH_FMT);
    }
}
