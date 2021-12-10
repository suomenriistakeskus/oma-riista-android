package fi.riista.common.groupHunting.ui.groupSelection

import fi.riista.common.groupHunting.model.HuntingGroupTarget
import fi.riista.common.model.SpeciesCode
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class SelectHuntingGroupViewModel(
    override val fields: DataFields<SelectHuntingGroupField>,
    val canCreateHarvest: Boolean,
    val canCreateObservation: Boolean,
    val proposedEventsCount: Int,
    val selectedSpecies: SpeciesCode?,

    /**
     * A target for the selected hunting group.
     */
    val selectedHuntingGroupTarget: HuntingGroupTarget?,
) : DataFieldViewModel<SelectHuntingGroupField>() {
    val huntingGroupSelected: Boolean
        get() = selectedHuntingGroupTarget != null
}
