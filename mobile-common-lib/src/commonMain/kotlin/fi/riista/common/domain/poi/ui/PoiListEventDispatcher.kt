package fi.riista.common.domain.poi.ui

import fi.riista.common.domain.poi.model.PoiLocationGroupId

interface PoiListEventDispatcher {
    fun dispatchPoiGroupSelected(groupId: PoiLocationGroupId)
}
