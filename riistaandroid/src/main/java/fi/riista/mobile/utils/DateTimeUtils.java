package fi.riista.mobile.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;

import fi.riista.mobile.AppConfig;

public class DateTimeUtils {

    /**
     * Check if date is in range (inclusive)
     *
     * @param validateDate Check date
     * @param beginDate    First date in range
     * @param endDate      Last day in range
     * @return Is date in range (includes begin and end dates)
     */
    static public boolean isDateInRange(LocalDate validateDate, LocalDate beginDate, LocalDate endDate) {
        if (validateDate == null || beginDate == null || endDate == null) {
            return false;
        }

        return !validateDate.isBefore(beginDate) && !validateDate.isAfter(endDate);
    }

    public static DateTime getHuntingYearStart(int year) {
        return new DateTime(year, 8, 1, 0, 0);
    }

    public static DateTime getHuntingYearEnd(int year) {
        return new DateTime(year + 1, 8, 1, 0, 0);
    }

    public static DateTime parseDate(String pointOfTime) {
        return DateTime.parse(pointOfTime, DateTimeFormat.forPattern(AppConfig.SERVER_DATE_FORMAT));
    }

    public static int getSeasonStartYearFromDate(Calendar calendar) {
        if (calendar.get(Calendar.MONTH) >= Calendar.AUGUST) {
            return calendar.get(Calendar.YEAR);
        } else {
            return calendar.get(Calendar.YEAR) - 1;
        }
    }
}
