package fi.riista.common.domain.harvest.ui.settings

import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

class HarvestSettingsEventToIntentMapper(
    private val intentHandler: IntentHandler<HarvestSettingsIntent>,
): HarvestSettingsEventDispatcher {

    override val booleanEventDispatcher = BooleanEventDispatcher<HarvestSettingsField> { fieldId, value ->
        val intent = when (fieldId) {
            HarvestSettingsField.HARVEST_FOR_OTHER_HUNTER -> HarvestSettingsIntent.ChangeOtherHuntersEnabled(value)
            HarvestSettingsField.ENABLE_CLUB_SELECTION -> HarvestSettingsIntent.ChangeClubSelectionEnabled(value)
        }

        intentHandler.handleIntent(intent)
    }
}
