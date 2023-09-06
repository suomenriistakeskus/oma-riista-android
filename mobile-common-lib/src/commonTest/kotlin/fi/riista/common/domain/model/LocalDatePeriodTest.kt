package fi.riista.common.domain.model

import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.PeriodDate
import fi.riista.common.model.isWithinPeriod
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

    @Test
    fun testLocalDateWithinOpenPeriod() {
        assertTrue(LocalDate(2021, 5, 10).isWithinPeriod(
            LocalDatePeriod(PeriodDate.DefinedDate(BEGIN_DATE), PeriodDate.OpenDate())
        ))
        assertTrue(LocalDate(2021, 5, 10).isWithinPeriod(
            LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.DefinedDate(END_DATE))
        ))
        assertTrue(LocalDate(2021, 5, 10).isWithinPeriod(
            LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.OpenDate())
        ))
    }

    @Test
    fun testLocalDatePeriodWithinOpenPeriod() {
        val period = createPeriod()
        assertTrue(period.isWithinPeriod(LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.DefinedDate(END_DATE))))
        assertTrue(period.isWithinPeriod(LocalDatePeriod(PeriodDate.DefinedDate(BEGIN_DATE), PeriodDate.OpenDate())))
        assertTrue(period.isWithinPeriod(LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.OpenDate())))
    }

    @Test
    fun testLocalDatePeriodOutsideOpenPeriod() {
        val period = createPeriod()
        assertFalse(period.isWithinPeriod(LocalDatePeriod(PeriodDate.DefinedDate(END_DATE), PeriodDate.OpenDate())))
        assertFalse(period.isWithinPeriod(LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.DefinedDate(BEGIN_DATE))))
    }

    @Test
    fun testLocalDateOutsideOpenPeriod() {
        assertFalse(LocalDate(2021, 5, 9).isWithinPeriod(
            LocalDatePeriod(PeriodDate.DefinedDate(BEGIN_DATE), PeriodDate.OpenDate())

        ))
        assertFalse(LocalDate(2021, 5, 13).isWithinPeriod(
            LocalDatePeriod(PeriodDate.OpenDate(), PeriodDate.DefinedDate(END_DATE))
        ))
    }

    private fun createPeriod(beginDate: LocalDate = BEGIN_DATE, endDate: LocalDate = END_DATE): LocalDatePeriod {
        return LocalDatePeriod(beginDate, endDate)
    }

    companion object {
        private val BEGIN_DATE = LocalDate(2021, 5, 10)
        private val END_DATE = LocalDate(2021, 5, 12)
    }
}
