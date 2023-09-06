package fi.riista.common.domain.model

import fi.riista.common.model.ETRMSGeoLocation
import kotlinx.serialization.Serializable

@Serializable
sealed class CommonLocation {
    @Serializable
    data class Known(val etrsLocation: ETRMSGeoLocation): CommonLocation()

    @Serializable
    object Unknown: CommonLocation()

    val etrsLocationOrNull: ETRMSGeoLocation?
        get() {
            return when (this) {
                is Known -> etrsLocation
                Unknown -> null
            }
        }
}

internal fun ETRMSGeoLocation.asKnownLocation() = CommonLocation.Known(etrsLocation = this)