package fi.riista.common.domain.poi.ui

import fi.riista.common.ui.intent.IntentHandler

internal class PoiLocationEventToIntentMapper(
    val intentHandler: IntentHandler<PoiLocationEventIntent>
) : PoiLocationEventDispatcher {
    override fun dispatchPoiLocationSelected(index: Int) {
        intentHandler.handleIntent(PoiLocationEventIntent.SelectPoiLocation(index))
    }
}

sealed class PoiLocationEventIntent {
    data class SelectPoiLocation(val index: Int) : PoiLocationEventIntent()
}
