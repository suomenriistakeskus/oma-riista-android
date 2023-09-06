package fi.riista.common.model

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class ETRSCoordinate(
    val x: Long,
    val y: Long,
)

/**
 * Calculates distance approximation in meters.
 *
 * More information: https://www.einouikkanen.fi/geodocs/OppijaksoKoordinaatistoista.html
 */
internal fun ETRSCoordinate.distanceTo(other: ETRSCoordinate): Double {
    // From provided link:
    // ETRS-TM35FIN coordinates provide us with a coordinate system as close as possible to a rectangular plane grid,
    // where the unit is a meter. Because of the curvature of the earth’s surface, this is not entirely possible,
    // but in a limited area, even in an area of the size of Finland, we get very close. Therefore,
    // ETRS-TM35FIN coordinates can, with reasonable accuracy, be used to perform calculations as if
    // they were rectangular grid coordinates.
    //
    // --> calculate approximate distance using Pythagorean theorem
    return sqrt ((x - other.x).toDouble().pow(2) + (y - other.y).toDouble().pow(2))
}
