package fi.riista.common.groupHunting.ui

import fi.riista.common.groupHunting.model.AcceptStatus
import fi.riista.common.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.groupHunting.model.HuntingGroupStatus

/**
 * Resolves which actions (edit/approve/reject) are available to a harvest.
 */
internal object HarvestActionResolver {
    fun canCreateHarvest(status: HuntingGroupStatus): Boolean {
        return status.canCreateHarvest
    }

    fun canEditHarvest(status: HuntingGroupStatus, harvest: GroupHuntingHarvestData): Boolean {
        // Harvest can be edited when it has been approved.
        return status.canEditDiaryEntry &&
               harvest.acceptStatus == AcceptStatus.ACCEPTED
    }

    fun canApproveHarvest(status: HuntingGroupStatus, harvest: GroupHuntingHarvestData): Boolean {
        // Harvest can be approved when it is not already approved.
        return status.canEditDiaryEntry &&
               harvest.acceptStatus != AcceptStatus.ACCEPTED
    }

    fun canRejectHarvest(status: HuntingGroupStatus, harvest: GroupHuntingHarvestData): Boolean {
        // Harvest can be rejected when it is not already rejected.
        return status.canEditDiaryEntry &&
               harvest.acceptStatus != AcceptStatus.REJECTED
    }
}
