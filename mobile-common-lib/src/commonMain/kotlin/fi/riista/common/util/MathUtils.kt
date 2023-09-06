package fi.riista.common.util

import kotlin.math.PI

internal fun deg2rad(deg: Double): Double {
    return deg * PI / 180.0
}

internal fun rad2deg(rad: Double): Double {
    return rad * 180 / PI
}
