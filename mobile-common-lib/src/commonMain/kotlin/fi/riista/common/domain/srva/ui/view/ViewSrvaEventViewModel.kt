package fi.riista.common.domain.srva.ui.view

import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.domain.srva.ui.modify.EditableSrvaEvent
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewSrvaEventViewModel internal constructor(
    internal val srvaEvent: CommonSrvaEventData,
    override val fields: DataFields<SrvaEventField>,
    val canEdit: Boolean,
) : DataFieldViewModel<SrvaEventField>() {

    /**
     * The editable SRVA event in case SRVA can be edited.
     */
    val editableSrvaEvent: EditableSrvaEvent?
        get() {
            return if (canEdit) {
                EditableSrvaEvent(srvaEventData = srvaEvent)
            } else {
                null
            }
        }
}
