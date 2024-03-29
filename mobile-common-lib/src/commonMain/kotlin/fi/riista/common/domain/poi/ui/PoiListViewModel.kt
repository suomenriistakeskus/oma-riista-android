package fi.riista.common.domain.poi.ui

import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.domain.poi.model.PointOfInterestType
import fi.riista.common.model.BackendEnum

sealed class PoiListItem {
    abstract val id: Long

    data class PoiGroupItem(
        override val id: Long,
        val text: String,
        val type: BackendEnum<PointOfInterestType>,
        val expanded: Boolean,
    ) : PoiListItem()

    data class PoiItem(
        override val id: Long,
        val text: String,
        val description: String?,
        val groupId: Long,
    ) : PoiListItem()

    data class Separator(
        override val id: Long,
    ) : PoiListItem()
}

data class PoiListViewModel(
    internal val locationGroups: List<PoiLocationGroup>,
    internal val allItems: List<PoiListItem>,
    val visibleItems: List<PoiListItem>,
)
