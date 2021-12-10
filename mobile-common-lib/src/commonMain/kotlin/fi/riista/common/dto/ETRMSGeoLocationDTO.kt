package fi.riista.common.dto

import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ETRMSGeoLocationDTO(
    val latitude: Int,
    val longitude: Int,
    val source: GeoLocationSourceDTO? = null,
    val accuracy: Double? = null,
    val altitude: Double? = null,
    val altitudeAccuracy: Double? = null,
)

internal fun ETRMSGeoLocationDTO.toETRMSGeoLocation() : ETRMSGeoLocation {
    return ETRMSGeoLocation(
            latitude = latitude,
            longitude = longitude,
            source = source.toBackendEnum(),
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
    )
}
