package fi.riista.common.domain.model

import fi.riista.common.model.Date
import fi.riista.common.model.LocalDate
import fi.riista.common.model.toLocalDateWithinHuntingYear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTest {

    @Test
    fun testComparingDates() {
        assertEquals(Date(5, 30), Date(5, 30))
        assertTrue(Date(4, 30) < Date(5, 30))
        assertTrue(Date(5, 29) < Date(5, 30))
        assertTrue(Date(5, 30) > Date(4, 30))
        assertTrue(Date(5, 30) > Date(5, 29))
    }

    @Test
    fun testConversionToLocalDate() {
        assertEquals(
            LocalDate(2021, 5, 30),
            Date(5, 30).toLocalDate(2021)
        )
    }

    @Test
    fun testConversionToLocalDateWithinHuntingYear() {
        assertEquals(
            LocalDate(2021, 8, 1),
            Date(8, 1).toLocalDateWithinHuntingYear(2021)
        )
        assertEquals(
            LocalDate(2021, 12, 31),
            Date(12, 31).toLocalDateWithinHuntingYear(2021)
        )
        assertEquals(
            LocalDate(2022, 1, 1),
            Date(1, 1).toLocalDateWithinHuntingYear(2021)
        )
        assertEquals(
            LocalDate(2022, 7, 31),
            Date(7, 31).toLocalDateWithinHuntingYear(2021)
        )
    }
}
