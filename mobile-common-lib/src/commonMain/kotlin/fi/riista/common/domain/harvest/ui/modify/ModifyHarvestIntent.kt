package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.model.*
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.model.*

sealed class ModifyHarvestIntent {
    class LaunchPermitSelection(val restrictToCurrentPermitNumber: Boolean): ModifyHarvestIntent()
    object ClearSelectedPermit: ModifyHarvestIntent()
    class SelectPermit(val permit: CommonHarvestPermit, val speciesCode: SpeciesCode?): ModifyHarvestIntent()
    class ChangeSpecimenAmount(val specimenAmount: Int?): ModifyHarvestIntent()
    class ChangeSpecies(val species: Species): ModifyHarvestIntent()
    class SetEntityImage(val image: EntityImage): ModifyHarvestIntent()
    class ChangeSpecimenData(val specimenData: SpecimenFieldDataContainer): ModifyHarvestIntent()
    class ChangeDescription(val description: String): ModifyHarvestIntent()
    class ChangeAdditionalInformation(val newAdditionalInformation: String): ModifyHarvestIntent()
    class ChangeGender(val newGender: Gender?): ModifyHarvestIntent()
    class ChangeDateAndTime(val newDateAndTime: LocalDateTime): ModifyHarvestIntent()
    class ChangeTime(val newTime: LocalTime): ModifyHarvestIntent()
    class ChangeHuntingDay(val huntingDayId: GroupHuntingDayId): ModifyHarvestIntent()
    class ChangeDeerHuntingType(val deerHuntingType: BackendEnum<DeerHuntingType>): ModifyHarvestIntent()
    class ChangeDeerHuntingOtherTypeDescription(val deerHuntingOtherTypeDescription: String): ModifyHarvestIntent()
    class ChangeAge(val newAge: GameAge?): ModifyHarvestIntent()
    class ChangeLocation(
        val newLocation: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): ModifyHarvestIntent()
    class ChangeGreySealHuntingMethod(val greySealHuntingMethod: BackendEnum<GreySealHuntingMethod>): ModifyHarvestIntent()
    class ChangeWildBoarFeedingPlace(val feedingPlace: Boolean): ModifyHarvestIntent()
    class ChangeIsTaigaBean(val isTaigaBeanGoose: Boolean): ModifyHarvestIntent()
    class ChangeNotEdible(val newNotEdible: Boolean): ModifyHarvestIntent()
    class ChangeWeight(val newWeight: Double?): ModifyHarvestIntent()
    class ChangeWeightEstimated(val newWeight: Double?): ModifyHarvestIntent()
    class ChangeWeightMeasured(val newWeight: Double?): ModifyHarvestIntent()
    class ChangeFitnessClass(val newFitnessClass: BackendEnum<GameFitnessClass>): ModifyHarvestIntent()
    class ChangeAntlersType(val newAntlersType: BackendEnum<GameAntlersType>): ModifyHarvestIntent()
    class ChangeAntlersWidth(val newAntlersWidth: Int?): ModifyHarvestIntent()
    class ChangeAntlerPointsLeft(val newAntlerPointsLeft: Int?): ModifyHarvestIntent()
    class ChangeAntlerPointsRight(val newAntlerPointsRight: Int?): ModifyHarvestIntent()
    class ChangeAntlersLost(val newAntlersLost: Boolean): ModifyHarvestIntent()
    class ChangeAntlersGirth(val newAntlersGirth: Int?): ModifyHarvestIntent()
    class ChangeAntlerShaftWidth(val newAntlerShaftWidth: Int?): ModifyHarvestIntent()
    class ChangeAntlersLength(val newAntlersLength: Int?): ModifyHarvestIntent()
    class ChangeAntlersInnerWidth(val newAntlersInnerWidth: Int?): ModifyHarvestIntent()
    class ChangeAlone(val newAlone: Boolean): ModifyHarvestIntent()
    class ChangeIsOwnHarvest(val isOwnHarvest: Boolean): ModifyHarvestIntent()
    class ChangeActor(val newActor: StringWithId): ModifyHarvestIntent()
    class ChangeActorHunterNumber(val hunterNumber: Int?): ModifyHarvestIntent()
    class ChangeSelectedClub(val newSelectedClub: StringWithId): ModifyHarvestIntent()
    class ChangeSelectedClubOfficialCode(val officialCode: Int?): ModifyHarvestIntent()
}
