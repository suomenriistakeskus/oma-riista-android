package fi.riista.common.dto

import fi.riista.common.model.Coordinate
import fi.riista.common.model.CoordinateBounds
import kotlinx.serialization.Serializable

@Serializable
data class CoordinateBoundsDTO(
    val minLng: Double,
    val minLat: Double,
    val maxLng: Double,
    val maxLat: Double,
)

fun CoordinateBoundsDTO.toCoordinateBounds() = CoordinateBounds(
        minCoordinate = Coordinate(latitude = minLat, longitude = minLng),
        maxCoordinate = Coordinate(latitude = maxLat, longitude = maxLng)
)