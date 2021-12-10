package fi.riista.common.poi.ui

import fi.riista.common.ui.intent.IntentHandler

internal class PoiEventToIntentMapper(
    val intentHandler: IntentHandler<PoiEventIntent>
) : PoiEventDispatcher {

    override fun dispatchPoiFilterChanged(newPoiFilter: PoiFilter) {
        intentHandler.handleIntent(PoiEventIntent.ChangePoiFilter(newPoiFilter))
    }
}

sealed class PoiEventIntent {
    data class ChangePoiFilter(val poiFilter: PoiFilter): PoiEventIntent()
}
