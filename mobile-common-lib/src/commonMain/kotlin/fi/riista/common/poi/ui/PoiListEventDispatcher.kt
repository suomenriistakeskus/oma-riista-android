package fi.riista.common.poi.ui

import fi.riista.common.poi.model.PoiLocationGroupId

interface PoiListEventDispatcher {
    fun dispatchPoiGroupSelected(groupId: PoiLocationGroupId)
}
