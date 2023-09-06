package fi.riista.common.domain.groupHunting.ui.groupHarvest.view

import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewGroupHarvestViewModel internal constructor(
    internal val harvestData: CommonHarvestData,
    override val fields: DataFields<CommonHarvestField>,
    val canEditHarvest: Boolean,
    val canApproveHarvest: Boolean,
    val canRejectHarvest: Boolean,
    val huntingGroupArea: HuntingGroupArea?,
) : DataFieldViewModel<CommonHarvestField>()
