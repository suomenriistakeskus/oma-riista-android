package fi.riista.common.model

import kotlinx.serialization.Serializable

/**
 * A coordinate in WGS84 coordinate system.
 */
@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)