package fi.riista.common.domain.groupHunting.ui.groupObservation.view

import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewGroupObservationViewModel(
    internal val observationData: GroupHuntingObservationData,
    override val fields: DataFields<GroupObservationField>,
    val canApproveObservation: Boolean,
    val canEditObservation: Boolean,
    val canRejectObservation: Boolean,
    val huntingGroupArea: HuntingGroupArea?,
) : DataFieldViewModel<GroupObservationField>()
