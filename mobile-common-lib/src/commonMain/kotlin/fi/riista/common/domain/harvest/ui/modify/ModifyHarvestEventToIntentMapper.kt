package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.ActionEventDispatcher
import fi.riista.common.ui.dataField.AgeEventDispatcher
import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.dataField.DoubleEventDispatcher
import fi.riista.common.ui.dataField.EntityImageDispatcher
import fi.riista.common.ui.dataField.GenderEventDispatcher
import fi.riista.common.ui.dataField.HuntingDayIdEventDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.LocalDateTimeEventDispatcher
import fi.riista.common.ui.dataField.LocalTimeEventDispatcher
import fi.riista.common.ui.dataField.LocationEventDispatcher
import fi.riista.common.ui.dataField.PermitEventDispatcher
import fi.riista.common.ui.dataField.SpeciesEventDispatcher
import fi.riista.common.ui.dataField.SpecimenDataEventDispatcher
import fi.riista.common.ui.dataField.StringEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

internal class ModifyHarvestEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifyHarvestIntent>,
): ModifyHarvestEventDispatcher {

    override val speciesEventDispatcher = SpeciesEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.SPECIES_CODE_AND_IMAGE -> ModifyHarvestIntent.ChangeSpecies(
                species = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Species", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val imageEventDispatcher = EntityImageDispatcher { image ->
        intentHandler.handleIntent(
            ModifyHarvestIntent.SetEntityImage(image)
        )
    }

    override val specimenEventDispatcher = SpecimenDataEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.SPECIMENS -> ModifyHarvestIntent.ChangeSpecimenData(
                specimenData = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Specimen", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringEventDispatcher = StringEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.ADDITIONAL_INFORMATION ->
                ModifyHarvestIntent.ChangeAdditionalInformation(value)
            CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION ->
                ModifyHarvestIntent.ChangeDeerHuntingOtherTypeDescription(value)
            CommonHarvestField.DESCRIPTION ->
                ModifyHarvestIntent.ChangeDescription(value)
            else -> throw createUnexpectedEventException(fieldId, "String", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringWithIdEventDispatcher = StringWithIdEventDispatcher<CommonHarvestField> { fieldId, newValue ->
        if (newValue.size != 1) {
            throw RuntimeException("Wrong number of values for field $fieldId (newValue: $newValue)")
        }

        val stringWithId = newValue[0]

        val intent = when (fieldId) {
            CommonHarvestField.GREY_SEAL_HUNTING_METHOD -> ModifyHarvestIntent.ChangeGreySealHuntingMethod(stringWithId.toBackendEnum())
            CommonHarvestField.DEER_HUNTING_TYPE -> ModifyHarvestIntent.ChangeDeerHuntingType(stringWithId.toBackendEnum())
            CommonHarvestField.ANTLERS_TYPE -> ModifyHarvestIntent.ChangeAntlersType(stringWithId.toBackendEnum())
            CommonHarvestField.FITNESS_CLASS -> ModifyHarvestIntent.ChangeFitnessClass(stringWithId.toBackendEnum())
            CommonHarvestField.ACTOR -> ModifyHarvestIntent.ChangeActor(stringWithId)
            CommonHarvestField.SELECTED_CLUB -> ModifyHarvestIntent.ChangeSelectedClub(stringWithId)
            else -> throw createUnexpectedEventException(fieldId, "StringWithId", newValue)
        }

        intentHandler.handleIntent(intent)
    }

    override val localDateTimeEventDispatcher = LocalDateTimeEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.DATE_AND_TIME -> ModifyHarvestIntent.ChangeDateAndTime(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val localTimeEventDispatcher = LocalTimeEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.HUNTING_DAY_AND_TIME -> ModifyHarvestIntent.ChangeTime(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val huntingDayEventDispatcher = HuntingDayIdEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.HUNTING_DAY_AND_TIME -> ModifyHarvestIntent.ChangeHuntingDay(value)
            else -> throw createUnexpectedEventException(fieldId, "GroupHuntingDayId", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val genderEventDispatcher = GenderEventDispatcher<CommonHarvestField> { _, value ->
        intentHandler.handleIntent(ModifyHarvestIntent.ChangeGender(value))
    }

    override val ageEventDispatcher = AgeEventDispatcher<CommonHarvestField> { _, value ->
        intentHandler.handleIntent(ModifyHarvestIntent.ChangeAge(value))
    }

    override val locationEventDispatcher = LocationEventDispatcher<CommonHarvestField> { _, value ->
        intentHandler.handleIntent(
            ModifyHarvestIntent.ChangeLocation(
                newLocation = value,
                locationChangedAfterUserInteraction = true,
            )
        )
    }

    override val booleanEventDispatcher = BooleanEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.SELECT_PERMIT ->
                if (value) {
                    ModifyHarvestIntent.LaunchPermitSelection(restrictToCurrentPermitNumber = false)
                } else {
                    ModifyHarvestIntent.ClearSelectedPermit
                }
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE -> ModifyHarvestIntent.ChangeWildBoarFeedingPlace(value)
            CommonHarvestField.IS_TAIGA_BEAN_GOOSE -> ModifyHarvestIntent.ChangeIsTaigaBean(value)
            CommonHarvestField.NOT_EDIBLE -> ModifyHarvestIntent.ChangeNotEdible(value)
            CommonHarvestField.ALONE -> ModifyHarvestIntent.ChangeAlone(value)
            CommonHarvestField.ANTLERS_LOST -> ModifyHarvestIntent.ChangeAntlersLost(value)
            CommonHarvestField.OWN_HARVEST -> ModifyHarvestIntent.ChangeIsOwnHarvest(value)
            else -> throw createUnexpectedEventException(fieldId, "Boolean", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val doubleEventDispatcher = DoubleEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.WEIGHT -> ModifyHarvestIntent.ChangeWeight(value)
            CommonHarvestField.WEIGHT_ESTIMATED -> ModifyHarvestIntent.ChangeWeightEstimated(value)
            CommonHarvestField.WEIGHT_MEASURED -> ModifyHarvestIntent.ChangeWeightMeasured(value)
            else -> throw createUnexpectedEventException(fieldId, "Double?", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val intEventDispatcher = IntEventDispatcher<CommonHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonHarvestField.SPECIMEN_AMOUNT -> ModifyHarvestIntent.ChangeSpecimenAmount(value)
            CommonHarvestField.ACTOR_HUNTER_NUMBER -> ModifyHarvestIntent.ChangeActorHunterNumber(value)
            CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE -> ModifyHarvestIntent.ChangeSelectedClubOfficialCode(value)
            CommonHarvestField.ANTLERS_WIDTH -> ModifyHarvestIntent.ChangeAntlersWidth(value)
            CommonHarvestField.ANTLER_POINTS_LEFT -> ModifyHarvestIntent.ChangeAntlerPointsLeft(value)
            CommonHarvestField.ANTLER_POINTS_RIGHT -> ModifyHarvestIntent.ChangeAntlerPointsRight(value)
            CommonHarvestField.ANTLERS_GIRTH -> ModifyHarvestIntent.ChangeAntlersGirth(value)
            CommonHarvestField.ANTLER_SHAFT_WIDTH -> ModifyHarvestIntent.ChangeAntlerShaftWidth(value)
            CommonHarvestField.ANTLERS_LENGTH -> ModifyHarvestIntent.ChangeAntlersLength(value)
            CommonHarvestField.ANTLERS_INNER_WIDTH -> ModifyHarvestIntent.ChangeAntlersInnerWidth(value)
            else -> throw createUnexpectedEventException(fieldId, "Int?", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val linkActionEventDispatcher = ActionEventDispatcher<CommonHarvestField> { fieldId ->
        val intent = when (fieldId) {
            CommonHarvestField.PERMIT_INFORMATION ->
                ModifyHarvestIntent.LaunchPermitSelection(restrictToCurrentPermitNumber = true)
            else -> throw createUnexpectedEventException(fieldId, "Action", null)
        }

        intentHandler.handleIntent(intent)
    }

    override val permitEventDispatcher = PermitEventDispatcher { permit, speciesCode ->
        intentHandler.handleIntent(ModifyHarvestIntent.SelectPermit(permit, speciesCode))
    }

    private fun createUnexpectedEventException(
        fieldId: CommonHarvestField,
        eventType: String,
        newValue: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (newValue: $newValue)")
    }
}

