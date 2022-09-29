package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.model.*
import fi.riista.common.model.*

sealed class ModifyGroupHarvestIntent {
    class ChangeAdditionalInformation(val newAdditionalInformation: String): ModifyGroupHarvestIntent()
    class ChangeGender(val newGender: Gender?): ModifyGroupHarvestIntent()
    class ChangeDateAndTime(val newDateAndTime: LocalDateTime): ModifyGroupHarvestIntent()
    class ChangeTime(val newTime: LocalTime): ModifyGroupHarvestIntent()
    class ChangeHuntingDay(val huntingDayId: GroupHuntingDayId): ModifyGroupHarvestIntent()
    class ChangeDeerHuntingType(val deerHuntingType: BackendEnum<DeerHuntingType>): ModifyGroupHarvestIntent()
    class ChangeDeerHuntingOtherTypeDescription(val deerHuntingOtherTypeDescription: String): ModifyGroupHarvestIntent()
    class ChangeAge(val newAge: GameAge?): ModifyGroupHarvestIntent()
    class ChangeLocation(
        val newLocation: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): ModifyGroupHarvestIntent()
    class ChangeNotEdible(val newNotEdible: Boolean): ModifyGroupHarvestIntent()
    class ChangeWeightEstimated(val newWeight: Double?): ModifyGroupHarvestIntent()
    class ChangeWeightMeasured(val newWeight: Double?): ModifyGroupHarvestIntent()
    class ChangeFitnessClass(val newFitnessClass: BackendEnum<GameFitnessClass>): ModifyGroupHarvestIntent()
    class ChangeAntlersType(val newAntlersType: BackendEnum<GameAntlersType>): ModifyGroupHarvestIntent()
    class ChangeAntlersWidth(val newAntlersWidth: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlerPointsLeft(val newAntlerPointsLeft: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlerPointsRight(val newAntlerPointsRight: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlersLost(val newAntlersLost: Boolean): ModifyGroupHarvestIntent()
    class ChangeAntlersGirth(val newAntlersGirth: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlerShaftWidth(val newAntlerShaftWidth: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlersLength(val newAntlersLength: Int?): ModifyGroupHarvestIntent()
    class ChangeAntlersInnerWidth(val newAntlersInnerWidth: Int?): ModifyGroupHarvestIntent()
    class ChangeAlone(val newAlone: Boolean): ModifyGroupHarvestIntent()
    class ChangeActor(val newActor: StringWithId): ModifyGroupHarvestIntent()
    class ChangeActorHunterNumber(val hunterNumber: Int?): ModifyGroupHarvestIntent()
}
