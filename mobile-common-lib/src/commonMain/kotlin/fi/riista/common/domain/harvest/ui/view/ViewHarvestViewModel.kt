package fi.riista.common.domain.harvest.ui.view

import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.modify.EditableHarvest
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewHarvestViewModel internal constructor(
    internal val harvest: CommonHarvestData,
    override val fields: DataFields<CommonHarvestField>,
    val canEdit: Boolean,
) : DataFieldViewModel<CommonHarvestField>() {

    /**
     * The editable harvest in case harvest can be edited.
     */
    val editableHarvest: EditableHarvest?
        get() {
            return if (canEdit) {
                EditableHarvest(harvest = harvest)
            } else {
                null
            }
        }
}