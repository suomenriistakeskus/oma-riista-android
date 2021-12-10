package fi.riista.common.ui.controller.selectString

import fi.riista.common.model.StringWithId
import fi.riista.common.ui.intent.IntentHandler

internal class SelectStringWithIdEventToIntentMapper(
    val intentHandler: IntentHandler<SelectStringWithIdIntent>
) : SelectStringWithIdEventDispatcher {

    override fun dispatchFilterChanged(filter: String) {
        intentHandler.handleIntent(SelectStringWithIdIntent.ChangeFilter(filter))
    }

    override fun dispatchSelectedValueChanged(value: StringWithId) {
        intentHandler.handleIntent(SelectStringWithIdIntent.SelectStringWithId(value))
    }
}

sealed class SelectStringWithIdIntent {
    data class ChangeFilter(val filter: String): SelectStringWithIdIntent()
    data class SelectStringWithId(val value: StringWithId): SelectStringWithIdIntent()
}
