package fi.riista.common.domain.huntingControl.ui.eventSelection

import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.ui.intent.IntentHandler

class SelectHuntingControlEventEventToIntentMapper(
    val intentHandler: IntentHandler<SelectHuntingControlEventIntent>
) : SelectHuntingControlEventDispatcher {

    override fun dispatchRhySelected(id: OrganizationId) {
        intentHandler.handleIntent(SelectHuntingControlEventIntent.SelectRhy(id))
    }
}

sealed class SelectHuntingControlEventIntent {
    class SelectRhy(val rhyId: OrganizationId) : SelectHuntingControlEventIntent()
}
