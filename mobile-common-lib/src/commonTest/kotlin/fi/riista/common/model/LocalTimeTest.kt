package fi.riista.common.model

import fi.riista.common.dto.toLocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalTimeTest {

    @Test
    fun testParsingValidLocalTimes() {
        assertEquals(LocalTime(0, 0, 0), "00:00".toLocalTime())
        assertEquals(LocalTime(0, 0, 0), "00:00:00".toLocalTime())
        assertEquals(LocalTime(8, 0, 0), "08:00".toLocalTime())
        assertEquals(LocalTime(8, 0, 0), "08:00:00".toLocalTime())
        assertEquals(LocalTime(9, 30, 0), "09:30".toLocalTime())
        assertEquals(LocalTime(9, 30, 0), "09:30:00".toLocalTime())
        assertEquals(LocalTime(10, 45, 30), "10:45:30".toLocalTime())
        assertEquals(LocalTime(23, 59, 59), "23:59:59".toLocalTime())
    }

    @Test
    fun testParsingInvalidLocalTimes() {
        assertNull("foo".toLocalTime())
        assertNull("-00:00:00".toLocalTime())
        assertNull("24:00:00".toLocalTime())
        assertNull("0:60:00".toLocalTime())
        assertNull("0:00:60".toLocalTime())
        assertNull("2021-01-01T06:00:00".toLocalTime())
    }

    @Test
    fun testComparingLocalTimes() {
        assertEquals(LocalTime(8, 5, 30), LocalTime(8, 5, 30))
        assertTrue(LocalTime(7, 5, 30) < LocalTime(8, 5, 30))
        assertTrue(LocalTime(8, 4, 30) < LocalTime(8, 5, 30))
        assertTrue(LocalTime(8, 5, 29) < LocalTime(8, 5, 30))

        assertTrue(LocalTime(8, 5, 30) > LocalTime(7, 5, 30))
        assertTrue(LocalTime(8, 5, 30) > LocalTime(8, 4, 30))
        assertTrue(LocalTime(8, 5, 30) > LocalTime(8, 5, 29))
    }

    @Test
    fun testConvertingToHoursAndMinutesString() {
        assertEquals("00:00", LocalTime(0, 0, 1).toHoursAndMinutesString())
        assertEquals("01:01", LocalTime(1, 1, 2).toHoursAndMinutesString())
        assertEquals("09:09", LocalTime(9, 9, 3).toHoursAndMinutesString())
        assertEquals("10:10", LocalTime(10, 10, 4).toHoursAndMinutesString())
        assertEquals("23:59", LocalTime(23, 59, 5).toHoursAndMinutesString())
    }

    @Test
    fun testMinutesUntil() {
        assertEquals(0, time().minutesUntil(time()))
        assertEquals(0, time().minutesUntil(time(second = 59)))
        assertEquals(1, time().minutesUntil(time(minute = 1)))
        assertEquals(1, time().minutesUntil(time(minute = 1, second = 59)))
        assertEquals(59, time().minutesUntil(time(minute = 59, second = 59)))
        assertEquals(59, time(hour = 1).minutesUntil(time(hour = 1, minute = 59, second = 59)))
        assertEquals(59, time(hour = 23).minutesUntil(time(hour = 23, minute = 59, second = 59)))
        assertEquals(60, time(hour = 0).minutesUntil(time(hour = 1)))
        assertEquals(119, time(hour = 0).minutesUntil(time(hour = 1, minute = 59, second = 59)))
        assertEquals(1439, time(hour = 0).minutesUntil(time(hour = 23, minute = 59, second = 59)))
    }

    private fun time(hour: Int = 0, minute: Int = 0, second: Int = 0) = LocalTime(hour, minute, second)

}
