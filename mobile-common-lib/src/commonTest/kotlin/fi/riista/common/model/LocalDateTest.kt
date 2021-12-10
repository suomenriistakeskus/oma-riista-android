package fi.riista.common.model

import fi.riista.common.dto.toLocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalDateTest {

    @Test
    fun testParsingLocalDates() {
        assertEquals(LocalDate(2021, 1, 1), "2021-01-01".toLocalDate())
        assertEquals(LocalDate(2021, 5, 30), "2021-05-30".toLocalDate())
        assertEquals(LocalDate(2021, 12, 31), "2021-12-31".toLocalDate())
    }

    @Test
    fun testComparingLocalDates() {
        assertEquals(LocalDate(2021, 5, 30), LocalDate(2021, 5, 30))
        assertTrue(LocalDate(2020, 5, 30) < LocalDate(2021, 5, 30))
        assertTrue(LocalDate(2021, 4, 30) < LocalDate(2021, 5, 30))
        assertTrue(LocalDate(2021, 5, 29) < LocalDate(2021, 5, 30))
        assertTrue(LocalDate(2021, 5, 30) > LocalDate(2020, 5, 30))
        assertTrue(LocalDate(2021, 5, 30) > LocalDate(2021, 4, 30))
        assertTrue(LocalDate(2021, 5, 30) > LocalDate(2021, 5, 29))
    }

    @Test
    fun testConvertingToString() {
        assertEquals("2021-01-01", LocalDate(2021,1,1).toString())
        assertEquals("2021-12-31", LocalDate(2021,12,31).toString())
    }

    @Test
    fun testMinDateReturnsEarlierDate() {
        val date1 = LocalDate(2021, 4, 30)
        val date2 = LocalDate(2021, 6, 3)

        assertEquals(date1, minDate(date1, date2))
        assertEquals(date1, minDate(date2, date1))
    }

    @Test
    fun testMinDateWithOtherDateNull() {
        val date = LocalDate(2021, 4, 30)

        assertEquals(date, minDate(date, null))
        assertEquals(date, minDate(null, date))
    }

    @Test
    fun testMinDateWithBothDatesNull() {
        assertFailsWith<AssertionError> {
            minDate(null, null)
        }
    }

    @Test
    fun testMaxDateReturnsLaterDate() {
        val date1 = LocalDate(2021, 4, 30)
        val date2 = LocalDate(2021, 6, 3)

        assertEquals(date2, maxDate(date1, date2))
        assertEquals(date2, maxDate(date2, date1))
    }

    @Test
    fun testMaxDateWithOtherDateNull() {
        val date = LocalDate(2021, 4, 30)

        assertEquals(date, maxDate(date, null))
        assertEquals(date, maxDate(null, date))
    }

    @Test
    fun testGettingDateWithoutYear() {
        assertEquals(Date(1, 1),
                     LocalDate(2021, 1, 1).getDateWithoutYear())
        assertEquals(Date(7, 24),
                     LocalDate(2021, 7, 24).getDateWithoutYear())
        assertEquals(Date(12, 31),
                     LocalDate(2021, 12, 31).getDateWithoutYear())
    }

    @Test
    fun testGettingHuntingYear() {
        assertEquals(2021, LocalDate(2021, 8, 1).getHuntingYear())
        assertEquals(2021, LocalDate(2021, 12, 31).getHuntingYear())
        assertEquals(2021, LocalDate(2022, 1, 1).getHuntingYear())
        assertEquals(2021, LocalDate(2022, 7, 31).getHuntingYear())
    }
}
