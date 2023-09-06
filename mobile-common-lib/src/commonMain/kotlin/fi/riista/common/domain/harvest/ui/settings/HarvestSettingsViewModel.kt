package fi.riista.common.domain.harvest.ui.settings

import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class HarvestSettingsViewModel internal constructor(
    internal val settings: HarvestSettingsData,
    override val fields: DataFields<HarvestSettingsField> = listOf(),
): DataFieldViewModel<HarvestSettingsField>()
