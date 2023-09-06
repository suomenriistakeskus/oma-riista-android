package fi.riista.common.domain.sun

import fi.riista.common.model.ETRSCoordinate
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.util.toWGS84Coordinate

object SunriseAndSunsetCalculator {

    /**
     * Calculates the sunrise and sunset.
     */
    fun calculateSunriseAndSunset(
        date: LocalDate,
        location: ETRSCoordinate
    ): SunriseAndSunset {
        /*
        The calculations are performed using formulas found in NOAA excel sheets
        - https://gml.noaa.gov/grad/solcalc/calcdetails.html

        It appears that calculated sunrise and sunset times vary depending on what is the timepoint they
        are calculated for. For example if we'd calculate the time for 26.3.2023 12:00 we might get a
        sunrise result of 7:04:11. If we instead calculated the sunrise using 26.3.2023 7:04 as a timepoint
        we might get the result of 7:04:49. When rounded that's already a one minute difference and the difference
        is larger at different points of year (multiple minutes even).

        Let's use a following strategy:
        1. calculate the initial sunrise and sunset based on 12:00
        2. calculate more accurate sunrise and sunset times based on initial result
        3. fallback to 00:00 (for sunrise) and 23:59 (for sunset) if initial calculation didn't produce results
            -> this allows better values e.g. when midnight sun begins / ends as calculations may produce some results
               at the day start / end and instead produce NaN results in the middle of the day
        */
        val initialCalculation = SunriseAndSunsetCalculation(
            coordinate = location.toWGS84Coordinate()
        ).calculate(
            dateTime = LocalDateTime(
                date = date,
                time = LocalTime(12, 0, 0)
            ),
            debugMsg = "initial sunrise / sunset"
        )

        val sunriseLocalDateTime = calculateSunrise(date, initialCalculation)
        val sunsetLocalDateTime = calculateSunset(date, initialCalculation)

        return SunriseAndSunset(
            location = location,
            sunrise = sunriseLocalDateTime?.time,
            sunset = sunsetLocalDateTime?.time
        )
    }

    private fun calculateSunrise(date: LocalDate, calculation: SunriseAndSunsetCalculation): LocalDateTime? {
        val initialResult = calculation.takeIf { it.sunriseLocalDateTime != null }
            // no sunrise from initial calculation (at noon) --> fallback to attempting again early in the morning
            ?: calculation.calculate(
                dateTime = LocalDateTime(
                    date = date,
                    time = LocalTime(0, 0, 0)
                ),
                debugMsg = "fallback sunrise"
            )


        return initialResult.sunriseLocalDateTime?.let {
            // attempt to get a better result by calculating again using the first calculation as basis
            //
            // DON'T FALLBACK to initial result. It is possible that e.g. midnight sun is not detected based
            // on initial calculation (it can report both sunrise and sunset) but when calculating again
            // at sunrise / sunset point they are no longer available
            // - see NOAA sheets for more information
            initialResult.calculate(
                dateTime = it,
                debugMsg = "accurate sunrise"
            ).sunriseLocalDateTime
        }
    }

    private fun calculateSunset(date: LocalDate, calculation: SunriseAndSunsetCalculation): LocalDateTime? {
        val initialResult = calculation.takeIf { it.sunsetLocalDateTime != null }
        // no sunset from initial calculation (at noon) --> fallback to attempting again late in the evening
            ?: calculation.calculate(
                dateTime = LocalDateTime(
                    date = date,
                    time = LocalTime(23, 59, 59)
                ),
                debugMsg = "fallback sunset"
            )


        return initialResult.sunsetLocalDateTime?.let {
            // attempt to get a better result by calculating again using the first calculation as basis
            //
            // DON'T FALLBACK to initial result. It is possible that e.g. midnight sun is not detected based
            // on initial calculation (it can report both sunrise and sunset) but when calculating again
            // at sunrise / sunset point they are no longer available
            // - see NOAA sheets for more information
            initialResult.calculate(
                dateTime = it,
                debugMsg = "accurate sunset"
            ).sunsetLocalDateTime
        }
    }
}

