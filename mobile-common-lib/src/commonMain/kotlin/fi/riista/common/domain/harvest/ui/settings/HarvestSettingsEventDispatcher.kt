package fi.riista.common.domain.harvest.ui.settings

import fi.riista.common.ui.dataField.BooleanEventDispatcher

interface HarvestSettingsEventDispatcher {
    val booleanEventDispatcher: BooleanEventDispatcher<HarvestSettingsField>
}
