package fi.riista.common.model.extensions

import fi.riista.common.model.LocalDate
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

    companion object {
        // values using epoch converter: https://www.epochconverter.com/ using GMT as timezone
        private val dateEpochSeconds = mapOf(
                LocalDate(2021, 8, 6) to 1628208000L,
                LocalDate(2000, 1, 1) to 946684800L,
                LocalDate(2030, 12, 31) to 1924905600L,
        )
    }
}
