package fi.riista.common.groupHunting.ui.groupObservation

import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.ui.groupHarvest.modify.ModifyGroupHarvestIntent
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringWithId

sealed class GroupObservationIntent {
    class ChangeTime(val newTime: LocalTime): GroupObservationIntent()
    class ChangeHuntingDay(val huntingDayId: GroupHuntingDayId): GroupObservationIntent()
    class ChangeLocation(
        val newLocation: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): GroupObservationIntent()
    class ChangeActor(val newActor: StringWithId): GroupObservationIntent()
    class ChangeActorHunterNumber(val hunterNumber: Int?): GroupObservationIntent()
    class ChangeMooselikeMaleAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeFemaleAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeCalfAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeFemale1CalfAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeFemale2CalfsAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeFemale3CalfsAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeFemale4CalfsAmount(val newAmount: Int?): GroupObservationIntent()
    class ChangeMooselikeUnknownSpecimenAmount(val newAmount: Int?): GroupObservationIntent()
}
