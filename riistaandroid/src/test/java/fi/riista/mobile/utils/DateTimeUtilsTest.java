package fi.riista.mobile.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static fi.riista.mobile.utils.DateTimeUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DateTimeUtilsTest {

    @Test
    public void testIsDateInRange() {
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 7, 1), ld(2018, 7, 1)));
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 6, 30), ld(2018, 7, 2)));
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 1, 1), ld(2018, 7, 1)));
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 7, 1), ld(2018, 12, 31)));
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 1, 1), ld(2018, 12, 31)));
        assertTrue(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2017, 12, 31), ld(2019, 1, 1)));

        assertFalse(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 7, 2), ld(2018, 7, 2)));
        assertFalse(DateTimeUtils.isDateInRange(ld(2018, 7, 1), ld(2018, 6, 30), ld(2018, 6, 30)));

        assertFalse(DateTimeUtils.isDateInRange(null, null, null));
        assertFalse(DateTimeUtils.isDateInRange(null, ld(0, 1, 1), null));
        assertFalse(DateTimeUtils.isDateInRange(null, null, ld(3000, 12, 31)));
        assertFalse(DateTimeUtils.isDateInRange(null, ld(0, 1, 1), ld(3000, 12, 31)));
    }

    @Test
    public void testParseDateTime_withMilliseconds() {
        final DateTime expected = dt(2018, 6, 22, 12, 34, 56, 789);
        final DateTime result = DateTimeUtils.parseDateTime("2018-06-22T12:34:56.789");

        assertEquals(expected, result);
    }

    @Test
    public void testParseDateTime_noMilliseconds() {
        final DateTime expected = dt(2018, 6, 22, 12, 34, 56, 0);
        final DateTime result = DateTimeUtils.parseDateTime("2018-06-22T12:34:56");

        assertEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testParseDateTime_forNull() {
        //noinspection ConstantConditions
        DateTimeUtils.parseDateTime(null);
    }

    @Test
    public void testThreadSafetyOf_parseDateTime_withMilliseconds() {
        final DateTime expected = dt(2018, 6, 22, 12, 34, 56, 789);

        testThreadSafetyOfParseDateTime("2018-06-22T12:34:56.789", expected);
    }

    @Test
    public void testThreadSafetyOf_parseDateTime_noMilliseconds() {
        final DateTime expected = dt(2018, 6, 22, 12, 34, 56, 0);

        testThreadSafetyOfParseDateTime("2018-06-22T12:34:56", expected);
    }

    @Test
    public void testParseCalendar_withMilliseconds() {
        final Calendar expected = getCalendar(2018, 6, 22, 12, 34, 56, 789);
        final Calendar result = DateTimeUtils.parseCalendar("2018-06-22T12:34:56.789");

        assertEquals(expected, result);
    }

    @Test
    public void testParseCalendar_noMilliseconds() {
        final Calendar expected = getCalendar(2018, 6, 22, 12, 34, 56, 0);
        final Calendar result = DateTimeUtils.parseCalendar("2018-06-22T12:34:56");

        assertEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testParseCalendar_forNull() {
        //noinspection ConstantConditions
        DateTimeUtils.parseCalendar(null);
    }

    @Test
    public void testThreadSafetyOf_parseCalendar_withMilliseconds() {
        final Calendar expected = getCalendar(2018, 6, 22, 12, 34, 56, 789);

        testThreadSafetyOfParseCalendar("2018-06-22T12:34:56.789", expected);
    }

    @Test
    public void testThreadSafetyOf_parseCalendar_noMilliseconds() {
        final Calendar expected = getCalendar(2018, 6, 22, 12, 34, 56, 0);

        testThreadSafetyOfParseCalendar("2018-06-22T12:34:56", expected);
    }

    private interface ParseDateTimeStringFunction<T> {
        T apply(String dateTimeString);
    }

    private void testThreadSafetyOfParseDateTime(final String dateTimeStr,
                                                 final DateTime expected) {

        testThreadSafetyOfParseDateTimeString(dateTimeStr, expected, DateTimeUtils::parseDateTime);
    }

    private void testThreadSafetyOfParseCalendar(final String dateTimeStr,
                                                 final Calendar expected) {

        testThreadSafetyOfParseDateTimeString(dateTimeStr, expected, str -> {
            return DateTimeUtils.parseCalendar(dateTimeStr);
        });
    }

    private <T> void testThreadSafetyOfParseDateTimeString(final String dateTimeStr,
                                                           final T expected,
                                                           final ParseDateTimeStringFunction<T> conversionFn) {

        final AtomicBoolean allParsedSuccessful = new AtomicBoolean(true);

        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final Runnable task = () -> {
            try {
                final T result = conversionFn.apply(dateTimeStr);

                if (!expected.equals(result)) {
                    allParsedSuccessful.set(false);
                }
            } catch (final RuntimeException e) {
                allParsedSuccessful.set(false);
            }
        };

        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }

        try {
            // Wait manually for all threads to finish.
            Thread.sleep(1000L);
        } catch (final InterruptedException e) {
            fail("Thread-safety test interrupted");
        }

        executorService.shutdown();

        assertTrue("Got incorrect parse result for date-time string when multiple concurrent threads",
                allParsedSuccessful.get());
    }

    @Test
    public void testFormatDate() {
        final Date date = getCalendar(2018, 6, 22, 12, 34, 56, 789).getTime();
        assertEquals("2018-06-22T12:34:56.789", DateTimeUtils.formatDate(date));

        assertNull(DateTimeUtils.formatDate(null));
    }

    @Test
    public void testFormatTime() {
        final DateTime dateTime = DateTime.parse("2020-04-04T18:25:14");
        assertEquals("18:25", DateTimeUtils.formatTime(dateTime));
        final DateTime nullDateTime = null;
        assertNull(DateTimeUtils.formatTime(nullDateTime));
    }

    @Test
    public void testFormatDateUsingFinnishFormat() {
        final Date date = getCalendar(2018, 6, 2, 12, 34, 56, 789).getTime();
        assertEquals("02.06.2018", DateTimeUtils.formatDateUsingFinnishFormat(date));

        assertNull(DateTimeUtils.formatDateUsingFinnishFormat(null));
    }

    @Test
    public void testFormatLocalDateUsingShortFinnishFormat() {
        assertEquals("2.6.2018", DateTimeUtils.formatLocalDateUsingShortFinnishFormat(ld(2018, 6, 2)));
        assertNull(DateTimeUtils.formatLocalDateUsingShortFinnishFormat(null));
    }

    @Test
    public void testFormatLocalDateUsingLongFinnishFormat() {
        assertEquals("02.06.2018", DateTimeUtils.formatLocalDateUsingLongFinnishFormat(ld(2018, 6, 2)));
        assertNull(DateTimeUtils.formatLocalDateUsingLongFinnishFormat(null));
    }

    @Test
    public void testConvertDateStringToFinnishFormat() {
        assertEquals("02.06.2018", DateTimeUtils.convertDateStringToFinnishFormat("2018-06-02"));

        // TODO Empty string is not a proper result for null/invalid input in a widely-used general
        //  purpose method. It is preferred to do empty string conversion in UI code instead.
        assertEquals("", DateTimeUtils.convertDateStringToFinnishFormat("2018-06-02T12:34:56.789"));
        assertEquals("", DateTimeUtils.convertDateStringToFinnishFormat("02.06.2018"));
        assertEquals("", DateTimeUtils.convertDateStringToFinnishFormat("2.6.2018"));
        assertEquals("", DateTimeUtils.convertDateStringToFinnishFormat("<garbage>"));

        try {
            //noinspection ConstantConditions
            DateTimeUtils.convertDateStringToFinnishFormat(null);

            // TODO Maybe not consistent with empty string output for invalid input.
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // NPE expected
        }
    }

    @Test
    public void testGetHuntingYearStart() {
        final DateTime start2018 = dt(2018, 8, 1, 0, 0, 0, 0);
        assertEquals(start2018, DateTimeUtils.getHuntingYearStart(2018));
    }

    @Test
    public void testGetHuntingYearEnd() {
        final DateTime end2018 = dt(2019, 8, 1, 0, 0, 0, 0);
        assertEquals(end2018, DateTimeUtils.getHuntingYearEnd(2018));
    }

    @Test
    public void testGetHuntingYearForDate() {
        assertEquals(2017, DateTimeUtils.getHuntingYearForDate(ld(2018, 7, 31)));
        assertEquals(2018, DateTimeUtils.getHuntingYearForDate(ld(2018, 8, 1)));

        try {
            //noinspection ConstantConditions
            DateTimeUtils.getHuntingYearForDate(null);
            fail("Should have thrown NPE");
        } catch (final NullPointerException npe) {
            // NPE expected
        }
    }

    @Test
    public void testGetHuntingYearForCalendar() {
        final Calendar cal1 = getCalendar(2018, 7, 31, 23, 59, 59, 999);
        final Calendar cal2 = getCalendar(2018, 8, 1, 0, 0, 0, 0);

        assertEquals(2017, DateTimeUtils.getHuntingYearForCalendar(cal1));
        assertEquals(2018, DateTimeUtils.getHuntingYearForCalendar(cal2));

        try {
            //noinspection ConstantConditions
            DateTimeUtils.getHuntingYearForCalendar(null);
            fail("Should have thrown NPE");
        } catch (final NullPointerException npe) {
            // NPE expected
        }
    }

    @Test
    public void testLd() {
        final int year = 2018;
        final int monthOfYear = 12;
        final int dayOfMonth = 31;

        assertEquals(new LocalDate(year, monthOfYear, dayOfMonth), ld(year, monthOfYear, dayOfMonth));
    }

    private static DateTime dt(final int year, final int month, final int dayOfMonth,
                               final int hourOfDay, final int minute, final int second, final int ms) {

        return new DateTime(year, month, dayOfMonth, hourOfDay, minute, second, ms, DateTimeUtils.JODA_TZ_FINLAND);
    }

    private static Calendar getCalendar(final int year, final int month, final int dayOfMonth,
                                        final int hourOfDay, final int minute, final int second, final int ms) {

        final Calendar cal = Calendar.getInstance(DateTimeUtils.JAVA_TZ_FINLAND);
        cal.set(year, month - 1, dayOfMonth, hourOfDay, minute, second); // 0-based month
        cal.set(Calendar.MILLISECOND, ms);
        return cal;
    }
}
