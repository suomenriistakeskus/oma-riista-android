package fi.riista.common.domain.groupHunting.ui

import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.groupHunting.model.HuntingGroupStatus

/**
 * Resolves which actions (edit/approve/reject) are available to a harvest.
 */
internal object HarvestActionResolver {
    fun canCreateHarvest(status: HuntingGroupStatus): Boolean {
        return status.canCreateHarvest
    }

    fun canEditHarvest(status: HuntingGroupStatus, harvest: CommonHarvestData): Boolean {
        // Harvest can be edited when it has been approved.
        return status.canEditHarvest &&
               harvest.acceptStatus == AcceptStatus.ACCEPTED
    }

    fun canApproveHarvest(status: HuntingGroupStatus, harvest: CommonHarvestData): Boolean {
        // Harvest can be approved when it is not already approved.
        return status.canEditHarvest &&
               harvest.acceptStatus != AcceptStatus.ACCEPTED
    }

    fun canRejectHarvest(status: HuntingGroupStatus, harvest: CommonHarvestData): Boolean {
        // Harvest can be rejected when it is not already rejected.
        return status.canEditHarvest &&
               harvest.acceptStatus != AcceptStatus.REJECTED
    }
}
