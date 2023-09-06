package fi.riista.common.util

import android.location.Location
import android.os.Build
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum

fun ETRMSGeoLocation.toLocation(): Location {
    return Location(source.rawBackendEnumValue ?: "").also { location ->
        val etrsCoordinate = CoordinateConverter.convertETRMStoWGS84(
                etrs_x = latitude.toLong(),
                etrs_y = longitude.toLong()
        )

        location.latitude = etrsCoordinate.latitude
        location.longitude = etrsCoordinate.longitude
        location.accuracy = accuracy?.toFloat() ?: 0.0f
        location.altitude = altitude ?: 0.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.verticalAccuracyMeters = altitudeAccuracy?.toFloat() ?: 0.0f
        }
    }
}

fun Location.toETRMSGeoLocation(source: GeoLocationSource): ETRMSGeoLocation {
    return toETRMSGeoLocation(source = source.toBackendEnum())
}

fun Location.toETRMSGeoLocation(source: BackendEnum<GeoLocationSource>): ETRMSGeoLocation {
    val coordinates = CoordinateConverter.convertWGS84toETRSTM35FIN(
        latitude = latitude,
        longitude = longitude
    )

    return ETRMSGeoLocation(
        latitude = coordinates.x.toInt(),
        longitude = coordinates.y.toInt(),
        source = source,
        accuracy = accuracy.takeIf { it != 0.0f }?.toDouble(),
        altitude = altitude.takeIf { it != 0.0 },
        altitudeAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            verticalAccuracyMeters.takeIf { it != 0.0f }?.toDouble()
        } else {
            null
        }
    )
}