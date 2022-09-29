package fi.riista.common.domain.poi.dto

import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.toETRMSGeoLocation
import fi.riista.common.model.BackendId
import kotlinx.serialization.Serializable

@Serializable
data class PoiLocationDTO(
    val id: BackendId,
    val poiId: Long,
    val description: String? = null,
    val visibleId: Int,
    val geoLocation: ETRMSGeoLocationDTO,
)

internal fun PoiLocationDTO.toPoiLocation(): PoiLocation {
    return PoiLocation(
        id = id,
        poiId = poiId,
        description = description,
        visibleId = visibleId,
        geoLocation = geoLocation.toETRMSGeoLocation(),
    )
}
