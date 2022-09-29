package fi.riista.common.domain.groupHunting.ui

import fi.riista.common.domain.constants.isDeerOrMoose
import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.domain.groupHunting.model.HuntingGroupStatus

internal object ObservationActionResolver {

    fun canEditObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be edited when it is has been approved.
        // Currently only moose observations can be edited
        return status.canEditObservation &&
               observation.acceptStatus == AcceptStatus.ACCEPTED &&
               observation.gameSpeciesCode.isMoose()
    }

    fun canApproveObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be approved when it is not yet approved.
        // Currently only moose observations can be approved
        return status.canEditObservation &&
               observation.acceptStatus != AcceptStatus.ACCEPTED &&
               observation.gameSpeciesCode.isMoose()
    }

    fun canRejectObservation(status: HuntingGroupStatus, observation: GroupHuntingObservationData): Boolean {
        // Observation can be rejected when it is not already rejected.
        // Currently only moose and deer observations can be rejected

        // note the usage of generic canEditDiaryEntry flag as that determines whether
        // rejecting entries is possible in general level. canEditObservation limits the
        // editing possibility for deer animals -> it would prevent rejecting deer observations.
        return status.canEditDiaryEntry &&
               observation.acceptStatus != AcceptStatus.REJECTED &&
               observation.gameSpeciesCode.isDeerOrMoose()
    }
}
