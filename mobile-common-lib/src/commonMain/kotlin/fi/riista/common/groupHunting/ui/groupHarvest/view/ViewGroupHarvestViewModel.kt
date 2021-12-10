package fi.riista.common.groupHunting.ui.groupHarvest.view

import fi.riista.common.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewGroupHarvestViewModel(
    internal val harvestData: GroupHuntingHarvestData,
    override val fields: DataFields<GroupHarvestField>,
    val canEditHarvest: Boolean,
    val canApproveHarvest: Boolean,
    val canRejectHarvest: Boolean,
) : DataFieldViewModel<GroupHarvestField>()
