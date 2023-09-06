package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifyHuntingControlEventViewModel(
    internal val event: HuntingControlEventData,
    internal val gameWardens: List<HuntingControlGameWarden>,
    internal val selfInspectorWarning: Boolean,

    override val fields: DataFields<HuntingControlEventField> = listOf(),
    val eventIsValid: Boolean = false,
) : DataFieldViewModel<HuntingControlEventField>()
