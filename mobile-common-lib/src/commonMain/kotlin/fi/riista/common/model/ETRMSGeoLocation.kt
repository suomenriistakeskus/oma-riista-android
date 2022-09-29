package fi.riista.common.model

import fi.riista.common.dto.ETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

enum class GeoLocationSource(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    GPS_DEVICE("GPS_DEVICE"),
    MANUAL("MANUAL"),
    ;

    // for iOS compatibility

    fun toBackendEnumCompat(): BackendEnum<GeoLocationSource> = toBackendEnum()
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<GeoLocationSource> = value.toBackendEnum()
    }
}

@Serializable
data class ETRMSGeoLocation(
    val latitude: Int,
    val longitude: Int,
    val source: BackendEnum<GeoLocationSource>,
    val accuracy: Double? = null,
    val altitude: Double? = null,
    val altitudeAccuracy: Double? = null,
)

internal fun ETRMSGeoLocation.toETRMSGeoLocationDTO() : ETRMSGeoLocationDTO {
    return ETRMSGeoLocationDTO(
            latitude = latitude,
            longitude = longitude,
            source = source.rawBackendEnumValue,
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
    )
}

fun ETRMSGeoLocation.isInsideFinland(): Boolean {
    // a rough bounding box for Finland. Coordinates from web frontend map.
    val longitudeRange = 61_000..733_500
    val latitudeRange = 6_600_000..7_778_000

    return longitude in longitudeRange && latitude in latitudeRange
}
