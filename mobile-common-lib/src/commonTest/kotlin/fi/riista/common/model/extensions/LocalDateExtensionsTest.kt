package fi.riista.common.model.extensions

import fi.riista.common.model.LocalDate
import fi.riista.common.model.toKotlinxLocalDate
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateExtensionsTest {
    @Test
    fun testConversionToEpochSeconds() {
        dateEpochSeconds.entries.forEach { (date, epochSeconds) ->
            assertEquals(epochSeconds, date.secondsFromEpoch(), "Date: $date")
        }
    }

    @Test
    fun testConversionFromEpochSeconds() {
        dateEpochSeconds.entries.forEach { (date, epochSeconds) ->
            assertEquals(date, LocalDate.fromEpochSeconds(epochSeconds), "Date: $date")
        }
    }

    @Test
    fun `julian day calculations`() {
        // example from https://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
        assertEquals(expected = 2299160.5, actual = LocalDate(1582, 10, 15).toJulianDay())

        val initialJulianDay = 2458849.5
        val initialDate = LocalDate(2020, 1, 1)

        for (days in 1 .. (365 * 10 + 10)) {
            val date = initialDate.plusDays(days)
            assertEquals(
                expected = initialJulianDay + days,
                actual = date.toJulianDay(),
                message = "date $date"
            )
        }
    }

    private fun LocalDate.plusDays(days: Int): LocalDate {
        val result = toKotlinxLocalDate().plus(days, DateTimeUnit.DAY)
        return LocalDate(result.year, result.monthNumber, result.dayOfMonth)
    }

    companion object {
        // values using epoch converter: https://www.epochconverter.com/ using GMT as timezone
        private val dateEpochSeconds = mapOf(
                LocalDate(2021, 8, 6) to 1628208000L,
                LocalDate(2000, 1, 1) to 946684800L,
                LocalDate(2030, 12, 31) to 1924905600L,
        )
    }
}
