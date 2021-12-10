package fi.riista.common.groupHunting.ui.groupObservation.view

import fi.riista.common.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewGroupObservationViewModel(
    internal val observationData: GroupHuntingObservationData,
    override val fields: DataFields<GroupObservationField>,
    val canApproveObservation: Boolean,
    val canEditObservation: Boolean,
    val canRejectObservation: Boolean,
) : DataFieldViewModel<GroupObservationField>()
