package fi.riista.common.domain.poi.dto

import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.model.BackendId
import fi.riista.common.model.Revision
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class PoiLocationGroupDTO(
    val id: BackendId,
    val rev: Revision,
    val visibleId: Int,
    val clubId: BackendId? = null,
    val description: String? = null,
    val type: PointOfInterestTypeDTO,
    val lastModifiedDate: LocalDateTimeDTO? = null,
    val lastModifierName: String? = null,
    val lastModifierRiistakeskus: Boolean,
    val locations: List<PoiLocationDTO>,
)

internal fun PoiLocationGroupDTO.toPoiLocationGroup(): PoiLocationGroup {
    return PoiLocationGroup(
        id = id,
        rev = rev,
        visibleId = visibleId,
        clubId = clubId,
        description = description,
        type = type.toBackendEnum(),
        lastModifiedDate = lastModifiedDate?.toLocalDateTime(),
        lastModifierName = lastModifierName,
        lastModifierRiistakeskus = lastModifierRiistakeskus,
        locations = locations.map { it.toPoiLocation() },
    )
}
