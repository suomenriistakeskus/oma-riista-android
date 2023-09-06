package fi.riista.common.domain.harvest.ui.settings

import fi.riista.common.ui.dataField.DataFieldId

enum class HarvestSettingsField : DataFieldId {
    HARVEST_FOR_OTHER_HUNTER,
    ENABLE_CLUB_SELECTION,
    ;

    override fun toInt() = ordinal
}
