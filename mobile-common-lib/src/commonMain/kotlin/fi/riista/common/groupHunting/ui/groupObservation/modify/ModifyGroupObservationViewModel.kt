package fi.riista.common.groupHunting.ui.groupObservation.modify

import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.groupHunting.model.HuntingGroupMember
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifyGroupObservationViewModel(
    internal val observation: GroupHuntingObservationData,
    internal val huntingGroupMembers: List<HuntingGroupMember>,
    override val fields: DataFields<GroupObservationField> = listOf(),
    internal val huntingDays: List<GroupHuntingDay>,
    val observationIsValid: Boolean = false,
) : DataFieldViewModel<GroupObservationField>()
