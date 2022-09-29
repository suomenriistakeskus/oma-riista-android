package fi.riista.common.domain.observation.ui.view

import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.modify.EditableObservation
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewObservationViewModel internal constructor(
    internal val observation: CommonObservationData,
    override val fields: DataFields<CommonObservationField>,
    val canEdit: Boolean,
) : DataFieldViewModel<CommonObservationField>() {

    /**
     * The editable observation in case observation can be edited.
     */
    val editableObservation: EditableObservation?
        get() {
            return if (canEdit) {
                EditableObservation(observation = observation)
            } else {
                null
            }
        }
}