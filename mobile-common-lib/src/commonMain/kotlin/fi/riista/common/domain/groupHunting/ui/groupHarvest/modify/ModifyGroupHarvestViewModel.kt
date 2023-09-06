package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifyGroupHarvestViewModel internal constructor(
    internal val harvest: CommonHarvestData,
    internal val huntingGroupStatus: HuntingGroupStatus,
    internal val huntingGroupMembers: List<HuntingGroupMember>,
    internal val huntingGroupPermit: HuntingGroupPermit,
    internal val huntingDays: List<GroupHuntingDay>,

    override val fields: DataFields<CommonHarvestField> = listOf(),
    val harvestIsValid: Boolean,
    val huntingGroupArea: HuntingGroupArea?,
): DataFieldViewModel<CommonHarvestField>()

