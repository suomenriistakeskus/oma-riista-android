package fi.riista.common.poi.ui

import fi.riista.common.poi.model.PoiLocationGroupId
import fi.riista.common.ui.intent.IntentHandler

internal class PoiListEventToIntentMapper(
    val intentHandler: IntentHandler<PoiListEventIntent>
) : PoiListEventDispatcher {
    override fun dispatchPoiGroupSelected(groupId: PoiLocationGroupId) {
        intentHandler.handleIntent(PoiListEventIntent.SelectPoiGroup(groupId))
    }
}

sealed class PoiListEventIntent {
    data class SelectPoiGroup(val id: PoiLocationGroupId) : PoiListEventIntent()
}
