package fi.riista.common.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalDatePeriodTest {

    @Test
    fun testLocalDateWithinPeriod() {
        assertTrue(LocalDate(2021, 5, 10).isWithinPeriod(createPeriod()))
        assertTrue(LocalDate(2021, 5, 11).isWithinPeriod(createPeriod()))
        assertTrue(LocalDate(2021, 5, 12).isWithinPeriod(createPeriod()))
    }

    @Test
    fun testLocalDateOutsideOfPeriod() {
        assertFalse(LocalDate(2021, 5, 9).isWithinPeriod(createPeriod()))
        assertFalse(LocalDate(2021, 5, 13).isWithinPeriod(createPeriod()))
    }

    @Test
    fun testLocalDatePeriodWithinPeriod() {
        assertTrue(createPeriod().isWithinPeriod(createPeriod()))
        assertTrue(createPeriod(beginDate = LocalDate(2021, 5, 11)).isWithinPeriod(createPeriod()))
        assertTrue(createPeriod(endDate = LocalDate(2021, 5, 11)).isWithinPeriod(createPeriod()))
    }

    @Test
    fun testLocalDatePeriodOutsideOfPeriod() {
        assertFalse(createPeriod(beginDate = LocalDate(2021, 5, 9)).isWithinPeriod(createPeriod()))
        assertFalse(createPeriod(endDate = LocalDate(2021, 5, 13)).isWithinPeriod(createPeriod()))
    }

    private fun createPeriod(beginDate: LocalDate = BEGIN_DATE, endDate: LocalDate = END_DATE): LocalDatePeriod {
        return LocalDatePeriod(beginDate, endDate)
    }

    companion object {
        private val BEGIN_DATE = LocalDate(2021, 5, 10)
        private val END_DATE = LocalDate(2021, 5, 12)
    }
}
