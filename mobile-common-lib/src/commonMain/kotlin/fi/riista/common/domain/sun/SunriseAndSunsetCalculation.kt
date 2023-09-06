package fi.riista.common.domain.sun

import fi.riista.common.logging.getLogger
import fi.riista.common.model.Coordinate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.dayStart
import fi.riista.common.model.plus
import fi.riista.common.model.toJulianDay
import fi.riista.common.model.toKotlinxLocalDateTime
import fi.riista.common.util.deg2rad
import fi.riista.common.util.rad2deg
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.tan

/**
 * Calculations based on https://gml.noaa.gov/grad/solcalc/calcdetails.html
 */
internal class SunriseAndSunsetCalculation(
    private val coordinate: Coordinate,
) {
    internal data class Preconditions(
        val localDateTime: LocalDateTime,
        val coordinate: Coordinate,
    ) {
        val lat: Double by lazy {
            deg2rad(coordinate.latitude)
        }

        val lonDeg: Double
            get() = coordinate.longitude
    }


    private var calculationPreconditions: Preconditions? = null
    private var calculated: Boolean = false

    private var sunriseMinuteOfDay: Double = Double.NaN
    val sunriseLocalDateTime: LocalDateTime?
        get() {
            require(calculated) { "Calculation not performed!" }
            return minutesToLocalDateTime(sunriseMinuteOfDay, "sunrise")
        }

    private var sunsetMinuteOfDay: Double = Double.NaN
    val sunsetLocalDateTime: LocalDateTime?
        get() {
            require(calculated) { "Calculation not performed!" }
            return minutesToLocalDateTime(sunsetMinuteOfDay, "sunrise")
        }

    fun calculate(dateTime: LocalDateTime, debugMsg: String): SunriseAndSunsetCalculation {
        val preconditions = Preconditions(dateTime, coordinate).also {
            calculationPreconditions = it
        }

        calculated = false

        logger.v { "Calculating $debugMsg using $preconditions" }

        val timezoneOffsetHours = determineTimezoneOffsetAt(
            // use day start as point-of-time when determining timezone offset. Doing this ensures that we get
            // correct calculations also when switching between summer and winter time
            // - LocalDateTime.toJulianDay() function already takes summertime saving into account and if we calculated
            //   offset based on noon / actual date time summertime saving could be taken into account twice
            // -> use day start as that way summertime saving is taken into account only once
            localDateTime = preconditions.localDateTime.date.dayStart()
        ).totalSeconds / 60.0 / 60.0

        val julianDay = preconditions.localDateTime.toJulianDay() - timezoneOffsetHours / 24.0
        val julianCentury = (julianDay - 2451545.0) / 36525.0

        val geomMeanLongSunDeg = (280.46646 + julianCentury * (36000.76983 + julianCentury * 0.0003032)) % 360
        val geomMeanLongSunRad = deg2rad(geomMeanLongSunDeg)
        val geomMeanAnomSunDeg = 357.52911 + julianCentury * (35999.05029 - 0.0001537 * julianCentury)
        val geomMeanAnomSunRad = deg2rad(geomMeanAnomSunDeg)

        val eccentEarthOrbit = 0.016708634 - julianCentury * (0.000042037 + 0.0000001267 * julianCentury)
        val sunEqOfCtrDeg = sin(geomMeanAnomSunRad) * (1.914602 - julianCentury * (0.004817 + 0.000014 * julianCentury)) +
                sin((2.0 * geomMeanAnomSunRad)) * (0.019993 - 0.000101 * julianCentury) +
                sin((3.0 * geomMeanAnomSunRad)) * 0.000289

        val sunTrueLongDeg = geomMeanLongSunDeg + sunEqOfCtrDeg

        val sunAppLongDeg = sunTrueLongDeg - 0.00569 - 0.00478 * sin(deg2rad(125.04 - 1934.136 * julianCentury))
        val sunAppLongRad = deg2rad(sunAppLongDeg)

        val meanObliqEclipticDeg = 23.0 + (26.0 + ((21.448 - julianCentury * (46.815 + julianCentury * (0.00059 - julianCentury * 0.001813))))/60.0)/60.0
        val obliqCorrDeg = meanObliqEclipticDeg + 0.00256 * cos(deg2rad(125.04 - 1934.136 * julianCentury))
        val oblicCorrRad = deg2rad(obliqCorrDeg)

        val sunDeclinDeg = rad2deg(asin(sin(oblicCorrRad) * sin(sunAppLongRad)))
        val sunDeclinRad = deg2rad(sunDeclinDeg)

        val varY = tan(oblicCorrRad / 2.0) * tan(oblicCorrRad / 2.0)
        val eqOfTimeMinutes = 4.0 * rad2deg(
            varY * sin(2.0 * geomMeanLongSunRad) -
                    2.0 * eccentEarthOrbit * sin(geomMeanAnomSunRad) +
                    4.0 * eccentEarthOrbit * varY * sin(geomMeanAnomSunRad) * cos(2.0 * geomMeanLongSunRad) -
                    0.5 * varY * varY * sin(4.0 * geomMeanLongSunRad) -
                    1.25 * eccentEarthOrbit * eccentEarthOrbit * sin(2.0 * geomMeanAnomSunRad)
        )
        val haSunriseDeg = rad2deg(
            acos(
                cos(deg2rad(90.833)) / (cos(preconditions.lat) * cos(sunDeclinRad)) -
                        tan(preconditions.lat) * tan(sunDeclinRad)
            )
        )

        val solarNoon = (720.0 - 4.0 * preconditions.lonDeg - eqOfTimeMinutes + timezoneOffsetHours * 60.0) / 1440.0
        val sunriseTime = solarNoon - haSunriseDeg * 4.0 / 1440.0
        val sunsetTime = solarNoon + haSunriseDeg * 4.0 / 1440.0

        sunriseMinuteOfDay = round((sunriseTime * 24.0) * 60.0)
        sunsetMinuteOfDay = round((sunsetTime * 24.0) * 60.0)

        calculated = true

        return this
    }

    private fun determineTimezoneOffsetAt(localDateTime: LocalDateTime): UtcOffset {
        val timezone = TimeZone.currentSystemDefault()
        val dateTimeInstant = localDateTime.toKotlinxLocalDateTime().toInstant(timezone)

        return TimeZone.currentSystemDefault().offsetAt(dateTimeInstant)
    }

    private fun minutesToLocalDateTime(minuteOfDay: Double, event: String): LocalDateTime? {
        if (minuteOfDay.isNaN()) {
            logger.v { "No minuteOfDay for $event" }
            return null
        }

        val preconditions = calculationPreconditions ?: kotlin.run {
            logger.w { "No calculation preconditions $event!" }
            return null
        }

        val dayStart = LocalDateTime(
            date = preconditions.localDateTime.date,
            time = LocalTime(0, 0, 0)
        )

        return dayStart.plus(minutes = minuteOfDay.toInt())
    }

    companion object {
        private val logger by getLogger(SunriseAndSunsetCalculation::class)
    }
}
