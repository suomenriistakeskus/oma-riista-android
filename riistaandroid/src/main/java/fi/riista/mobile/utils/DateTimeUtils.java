package fi.riista.mobile.utils;

import static java.util.Objects.requireNonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

    public static final TimeZone JAVA_TZ_FINLAND = TimeZone.getTimeZone(ConstantsKt.TIMEZONE_ID_FINLAND);
    public static final DateTimeZone JODA_TZ_FINLAND = DateTimeZone.forID(ConstantsKt.TIMEZONE_ID_FINLAND);

    private static final DateTimeFormatter DATETIME_FMT_DEFAULT =
            DateTimeFormat.forPattern(ConstantsKt.ISO_8601).withLocale(Locales.FI);
    private static final DateTimeFormatter DATETIME_FMT_NO_MILLISECONDS =
            DateTimeFormat.forPattern(ConstantsKt.ISO_8601_NO_MILLISECONDS).withLocale(Locales.FI);

    private static final DateTimeFormatter DATE_FMT_DEFAULT =
            DateTimeFormat.forPattern(ConstantsKt.DATE_FORMAT_STD).withLocale(Locales.FI);
    private static final DateTimeFormatter DATE_FMT_FINNISH_SHORT =
            DateTimeFormat.forPattern(ConstantsKt.DATE_FORMAT_FINNISH_SHORT).withLocale(Locales.FI);
    private static final DateTimeFormatter DATE_FMT_FINNISH_LONG =
            DateTimeFormat.forPattern(ConstantsKt.DATE_FORMAT_FINNISH_LONG).withLocale(Locales.FI);

    private static final DateTimeFormatter TIME_FMT_FINNISH =
            DateTimeFormat.forPattern(ConstantsKt.TIME_FORMAT).withLocale(Locales.FI);

    private static final int HUNTING_YEAR_BEGIN_MONTH = 8;

    /**
     * Check if date is in range (inclusive)
     *
     * @param validateDate Check date
     * @param beginDate    First date in range
     * @param endDate      Last day in range
     * @return Is date in range (includes begin and end dates)
     */
    public static boolean isDateInRange(@Nullable final LocalDate validateDate,
                                        @Nullable final LocalDate beginDate,
                                        @Nullable final LocalDate endDate) {

        if (validateDate == null || beginDate == null || endDate == null) {
            return false;
        }

        return !validateDate.isBefore(beginDate) && !validateDate.isAfter(endDate);
    }

    /**
     * Parses a Joda DateTime object from an input string.
     *
     * @param dateTimeStr input string to be parsed
     *
     * @return DateTime object parsed from input
     *
     * @exception NullPointerException if dateTimeStr is null
     * @exception IllegalArgumentException if dateTimeStr cannot be parsed
     */
    @NonNull
    public static DateTime parseDateTime(@NonNull final String dateTimeStr) {
        try {
            return DATETIME_FMT_DEFAULT.parseDateTime(dateTimeStr);
        } catch (final IllegalArgumentException ex) {
            return DATETIME_FMT_NO_MILLISECONDS.parseDateTime(dateTimeStr);
        }
    }

    @NonNull
    public static LocalDate parseLocalDateISO8601(@NonNull final String localDateStr) {
        try {
            return DATE_FMT_DEFAULT.parseLocalDate(localDateStr);
        } catch (final IllegalArgumentException ex) {
            throw ex;
        }

    }

    /**
     * Parses a java.util.Calendar object from an input string.
     *
     * @param dateTimeStr input string to be parsed
     *
     * @return Calendar object parsed from input
     *
     * @exception NullPointerException if dateTimeStr is null
     * @exception IllegalArgumentException if dateTimeStr cannot be parsed
     */
    @NonNull
    public static Calendar parseCalendar(@NonNull final String dateTimeStr) {
        final DateTime dateTime = parseDateTime(dateTimeStr);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        return calendar;
    }

    @Nullable
    public static String formatDateTime(@Nullable final DateTime dateTime) {
        return dateTime == null ? null : DATETIME_FMT_DEFAULT.print(dateTime);
    }

    @Nullable
    public static String formatDate(@Nullable final Date date) {
        return date == null ? null : DATETIME_FMT_DEFAULT.print(date.getTime());
    }

    @Nullable
    public static String formatTime(@Nullable final DateTime dateTime) {
        return dateTime == null ? null : TIME_FMT_FINNISH.print(dateTime);
    }

    @Nullable
    public static String formatTime(@Nullable final LocalTime time) {
        return time == null ? null : TIME_FMT_FINNISH.print(time);
    }

    @Nullable
    public static String formatDateUsingFinnishFormat(@Nullable final Date date) {
        return date == null ? null : DATE_FMT_FINNISH_LONG.print(date.getTime());
    }

    @Nullable
    public static String formatLocalDateUsingShortFinnishFormat(@Nullable final LocalDate date) {
        return date == null ? null : DATE_FMT_FINNISH_SHORT.print(date);
    }

    @Nullable
    public static String formatLocalDateUsingLongFinnishFormat(@Nullable final LocalDate date) {
        return date == null ? null : DATE_FMT_FINNISH_LONG.print(date);
    }

    @Nullable
    public static String formatLocalDateISO8601(@Nullable final LocalDate date) {
        return date == null ? null : DATE_FMT_DEFAULT.print(date);
    }

    /**
     * Tries to parse input string and convert the parsed date into long Finnish format.
     *
     * @param dateStr Input string to be parsed. Expected to be in ISO8601 format.
     *
     * @return either date string in long Finnish format or empty string if input cannot be parsed
     */
    @NonNull
    public static String convertDateStringToFinnishFormat(@NonNull final String dateStr) {
        requireNonNull(dateStr);

        String converted = null;

        try {
            converted = formatLocalDateUsingLongFinnishFormat(DATE_FMT_DEFAULT.parseLocalDate(dateStr));
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }

        // TODO Empty string is not a proper result for null/invalid input in a widely-used general
        //  purpose method. It is preferred to do empty string conversion in UI code instead.
        return converted != null ? converted : "";
    }

    // Inclusive point of time.
    @NonNull
    public static DateTime getHuntingYearStart(final int year) {
        return new DateTime(year, HUNTING_YEAR_BEGIN_MONTH, 1, 0, 0);
    }

    // Exclusive point of time.
    @NonNull
    public static DateTime getHuntingYearEnd(final int year) {
        return getHuntingYearStart(year + 1);
    }

    public static int getHuntingYearForDate(@NonNull final LocalDate date) {
        final int year = date.getYear();
        return date.getMonthOfYear() >= HUNTING_YEAR_BEGIN_MONTH ? year : year - 1;
    }

    public static int getHuntingYearForCalendar(@NonNull final Calendar calendar) {
        final int year = calendar.get(Calendar.YEAR);
        return calendar.get(Calendar.MONTH) >= (/* 0-based */ HUNTING_YEAR_BEGIN_MONTH - 1) ? year : year - 1;
    }

    public static LocalDate ld(final int year, final int monthOfYear, final int dayOfMonth) {
        return new LocalDate(year, monthOfYear, dayOfMonth);
    }

//    public static boolean overlapsInclusive(
//            @Nullable final Date begin, @Nullable final Date end, final LocalDate date) {
//
//        return overlapsInclusive(toLocalDateNullSafe(begin), toLocalDateNullSafe(end), date);
//    }
//
//    public static boolean overlapsInclusive(@Nullable final LocalDate begin, @Nullable final LocalDate end, final LocalDate date) {
//
//        return begin == null && end == null || dateRange(begin, end).contains(date);
//    }
//
//    public static boolean overlapsInclusive(@Nullable final LocalDate begin,
//                                            @Nullable final LocalDate end,
//                                            @Nullable final LocalDate begin2,
//                                            @Nullable final LocalDate end2) {
//
//        return begin == null && end == null ||
//                begin2 == null && end2 == null ||
//                dateRange(begin, end).isConnected(dateRange(begin2, end2));
//    }
//
//    @Nullable
//    public static LocalDate toLocalDateNullSafe(@Nullable final Date date) {
//        return date == null ? null : new LocalDate(date, Constants.DEFAULT_TIMEZONE);
//    }
//
//    @Nullable
//    public static LocalDateTime toLocalDateTimeNullSafe(@Nullable final Date date) {
//        return date == null ? null : new LocalDateTime(date, Constants.DEFAULT_TIMEZONE);
//    }
//
//    private static Range<LocalDate> dateRange(@Nullable final LocalDate begin, @Nullable final LocalDate end) {
//        if (begin == null) {
//            return end == null ? Range.all() : Range.atMost(end);
//        } else if (end == null) {
//            return Range.atLeast(begin);
//        }
//
//        return begin.isBefore(end) ? Range.closed(begin, end) : Range.closed(end, begin);
//    }
}
