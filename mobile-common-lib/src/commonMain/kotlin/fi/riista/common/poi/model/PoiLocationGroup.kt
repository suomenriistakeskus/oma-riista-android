package fi.riista.common.poi.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime

typealias PoiLocationGroupId = Long

data class PoiLocationGroup(
    val id: PoiLocationGroupId,
    val rev: Int,
    val visibleId: Int,
    val clubId: Long? = null,
    val description: String? = null,
    val type: BackendEnum<PointOfInterestType>,
    val lastModifiedDate: LocalDateTime? = null,
    val lastModifierName: String? = null,
    val lastModifierRiistakeskus: Boolean,
    val locations: List<PoiLocation>,
)
