package fi.riista.common.groupHunting.ui

import fi.riista.common.groupHunting.model.AcceptStatus
import fi.riista.common.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.groupHunting.model.HuntingGroupStatus
import fi.riista.common.model.isDeerOrMoose

internal object ObservationActionResolver {

    fun canEditObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be edited when it is has been approved.
        // Currently editing only moose and deer observations are supported
        return status.canEditDiaryEntry &&
               observation.acceptStatus == AcceptStatus.ACCEPTED &&
               observation.gameSpeciesCode.isDeerOrMoose()
    }

    fun canApproveObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be approved when it is not yet approved.
        // Currently accepting only moose and deer observations are supported
        return status.canEditDiaryEntry &&
               observation.acceptStatus != AcceptStatus.ACCEPTED &&
               observation.gameSpeciesCode.isDeerOrMoose()
    }

    fun canRejectObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be rejected when it is not already rejected.
        // Currently rejecting only moose and deer observations are supported
        return status.canEditDiaryEntry &&
               observation.acceptStatus != AcceptStatus.REJECTED &&
               observation.gameSpeciesCode.isDeerOrMoose()
    }
}
