package fi.riista.common.domain.groupHunting.ui.groupHarvest.view

import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewGroupHarvestViewModel(
    internal val harvestData: GroupHuntingHarvestData,
    override val fields: DataFields<GroupHarvestField>,
    val canEditHarvest: Boolean,
    val canApproveHarvest: Boolean,
    val canRejectHarvest: Boolean,
    val huntingGroupArea: HuntingGroupArea?,
) : DataFieldViewModel<GroupHarvestField>()
