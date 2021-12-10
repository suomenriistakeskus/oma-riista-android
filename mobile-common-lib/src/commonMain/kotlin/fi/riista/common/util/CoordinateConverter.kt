package fi.riista.common.util

import fi.riista.common.model.Coordinate
import fi.riista.common.model.ETRSCoordinate
import kotlin.math.*

/**
 * Helper class that provides coordinate conversion between WGS84 and ETRS-TM35FIN
 * The formulas can be found from http://docs.jhs-suositukset.fi/jhs-suositukset/JHS154_liite1/JHS154_liite1.html
 */
@Suppress("LocalVariableName", "MemberVisibilityCanBePrivate")
object CoordinateConverter {
    private const val Ca = 6378137.0
    private const val Cb = 6356752.314245
    private const val Cf = 1.0 / 298.257223563
    private const val Cn = Cf / (2.0 - Cf)
    private val CA1 = Ca / (1.0 + Cn) * (1.0 + Cn.pow(2.0) / 4.0 + Cn.pow(4.0) / 64.0)
    private val Ch1 = 1.0 / 2.0 * Cn - 2.0 / 3.0 * Cn.pow(2.0) +
            37.0 / 96.0 * Cn.pow(3.0) - 1.0 / 360.0 * Cn.pow(4.0)
    private val Ch2 =
        1.0 / 48.0 * Cn.pow(2.0) + 1.0 / 15.0 * Cn.pow(3.0) - 437.0 / 1440.0 * Cn.pow(4.0)
    private val Ch3 = 17.0 / 480.0 * Cn.pow(3.0) - 37.0 / 840.0 * Cn.pow(4.0)
    private val Ch4 = 4397.0 / 161280.0 * Cn.pow(4.0)
    private val Ch1p = 1.0 / 2.0 * Cn - 2.0 / 3.0 * Cn.pow(2.0) +
            5.0 / 16.0 * Cn.pow(3.0) + 41.0 / 180.0 * Cn.pow(4.0)
    private val Ch2p =
        13.0 / 48.0 * Cn.pow(2.0) - 3.0 / 5.0 * Cn.pow(3.0) + 557.0 / 1440.0 * Cn.pow(4.0)
    private val Ch3p = 61.0 / 240.0 * Cn.pow(3.0) - 103.0 / 140.0 * Cn.pow(4.0)
    private val Ch4p = 49561.0 / 161280.0 * Cn.pow(4.0)
    private val Ce = sqrt(2.0 * Cf - Cf.pow(2.0))
    private const val Ck0 = 0.9996
    private val Clo0 = deg2rad(27.0)
    private const val CE0 = 500000.0

    fun convertWGS84toETRSTM35FIN(latitude: Double, longitude: Double): ETRSCoordinate {
        val la = deg2rad(latitude)
        val lo = deg2rad(longitude)
        val Q = asinh(tan(la)) - Ce * atanh(Ce * sin(la))
        val be = atan(sinh(Q))
        val nnp = atanh(cos(be) * sin(lo - Clo0))
        val Ep = asin(sin(be) * cosh(nnp))
        val E1 = Ch1p * sin(2.0 * Ep) * cosh(2.0 * nnp)
        val E2 = Ch2p * sin(4.0 * Ep) * cosh(4.0 * nnp)
        val E3 = Ch3p * sin(6.0 * Ep) * cosh(6.0 * nnp)
        val E4 = Ch4p * sin(8.0 * Ep) * cosh(8.0 * nnp)
        val nn1 = Ch1p * cos(2.0 * Ep) * sinh(2.0 * nnp)
        val nn2 = Ch2p * cos(4.0 * Ep) * sinh(4.0 * nnp)
        val nn3 = Ch3p * cos(6.0 * Ep) * sinh(6.0 * nnp)
        val nn4 = Ch4p * cos(8.0 * Ep) * sinh(8.0 * nnp)
        val E = Ep + E1 + E2 + E3 + E4
        val nn = nnp + nn1 + nn2 + nn3 + nn4
        val etrs_x = (CA1 * E * Ck0).toLong()
        val etrs_y = (CA1 * nn * Ck0 + CE0).toLong()

        return ETRSCoordinate(x = etrs_x, y = etrs_y)
    }

    fun convertETRMStoWGS84(etrs_x: Long, etrs_y: Long): Coordinate {
        val E = etrs_x / (CA1 * Ck0)
        val nn = (etrs_y - CE0) / (CA1 * Ck0)
        val E1p = Ch1 * sin(2.0 * E) * cosh(2.0 * nn)
        val E2p = Ch2 * sin(4.0 * E) * cosh(4.0 * nn)
        val E3p = Ch3 * sin(6.0 * E) * cosh(6.0 * nn)
        val E4p = Ch4 * sin(8.0 * E) * cosh(8.0 * nn)
        val nn1p = Ch1 * cos(2.0 * E) * sinh(2.0 * nn)
        val nn2p = Ch2 * cos(4.0 * E) * sinh(4.0 * nn)
        val nn3p = Ch3 * cos(6.0 * E) * sinh(6.0 * nn)
        val nn4p = Ch4 * cos(8.0 * E) * sinh(8.0 * nn)
        val Ep = E - E1p - E2p - E3p - E4p
        val nnp = nn - nn1p - nn2p - nn3p - nn4p
        val be = asin(sin(Ep) / cosh(nnp))
        val Q = asinh(tan(be))

        var Qp = Q + Ce * atanh(Ce * tanh(Q))
        Qp = Q + Ce * atanh(Ce * tanh(Qp))
        Qp = Q + Ce * atanh(Ce * tanh(Qp))
        Qp = Q + Ce * atanh(Ce * tanh(Qp))
        val latitude = rad2deg(atan(sinh(Qp)))
        val longitude = rad2deg(Clo0 + asin(tanh(nnp) / cos(be)))

        return Coordinate(
                latitude = latitude,
                longitude = longitude
        )
    }

    private fun deg2rad(deg: Double): Double {
        return deg * PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180 / PI
    }
}

fun Coordinate.toETRSCoordinate() = CoordinateConverter.convertWGS84toETRSTM35FIN(latitude, longitude)
fun ETRSCoordinate.toWGS84Coordinate() = CoordinateConverter.convertETRMStoWGS84(x, y)
