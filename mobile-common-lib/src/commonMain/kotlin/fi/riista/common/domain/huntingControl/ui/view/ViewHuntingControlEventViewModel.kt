package fi.riista.common.domain.huntingControl.ui.view

import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

class ViewHuntingControlEventViewModel(
    internal val huntingControlEvent: HuntingControlEvent,
    override val fields: DataFields<HuntingControlEventField>,
    val canEditHuntingControlEvent: Boolean,
) : DataFieldViewModel<HuntingControlEventField>()
