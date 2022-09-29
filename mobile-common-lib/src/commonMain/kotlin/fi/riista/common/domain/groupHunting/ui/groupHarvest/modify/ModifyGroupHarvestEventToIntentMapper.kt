package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.*
import fi.riista.common.ui.intent.IntentHandler

internal class ModifyGroupHarvestEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifyGroupHarvestIntent>,
): ModifyGroupHarvestEventDispatcher {
    override val stringEventDispatcher = StringEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.ADDITIONAL_INFORMATION ->
                ModifyGroupHarvestIntent.ChangeAdditionalInformation(value)
            GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION ->
                ModifyGroupHarvestIntent.ChangeDeerHuntingOtherTypeDescription(value)
            else -> throw createUnexpectedEventException(fieldId, "String", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringWithIdEventDispatcher = StringWithIdEventDispatcher<GroupHarvestField> { fieldId, newValue ->
        if (newValue.size != 1) {
            throw RuntimeException("Wrong number of values for field $fieldId (newValue: $newValue)")
        }
        val intent = when (fieldId) {
            GroupHarvestField.DEER_HUNTING_TYPE -> ModifyGroupHarvestIntent.ChangeDeerHuntingType(newValue[0].toBackendEnum())
            GroupHarvestField.ANTLERS_TYPE -> ModifyGroupHarvestIntent.ChangeAntlersType(newValue[0].toBackendEnum())
            GroupHarvestField.FITNESS_CLASS -> ModifyGroupHarvestIntent.ChangeFitnessClass(newValue[0].toBackendEnum())
            GroupHarvestField.ACTOR -> ModifyGroupHarvestIntent.ChangeActor(newValue[0])
            else -> throw createUnexpectedEventException(fieldId, "StringWithId", newValue)
        }

        intentHandler.handleIntent(intent)
    }

    override val localDateTimeEventDispatcher = LocalDateTimeEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.DATE_AND_TIME -> ModifyGroupHarvestIntent.ChangeDateAndTime(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val localTimeEventDispatcher = LocalTimeEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.HUNTING_DAY_AND_TIME -> ModifyGroupHarvestIntent.ChangeTime(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val huntingDayEventDispatcher = HuntingDayIdEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.HUNTING_DAY_AND_TIME -> ModifyGroupHarvestIntent.ChangeHuntingDay(value)
            else -> throw createUnexpectedEventException(fieldId, "GroupHuntingDayId", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val genderEventDispatcher = GenderEventDispatcher<GroupHarvestField> { _, value ->
        intentHandler.handleIntent(ModifyGroupHarvestIntent.ChangeGender(value))
    }

    override val ageEventDispatcher = AgeEventDispatcher<GroupHarvestField> { _, value ->
        intentHandler.handleIntent(ModifyGroupHarvestIntent.ChangeAge(value))
    }

    override val locationEventDispatcher = LocationEventDispatcher<GroupHarvestField> { _, value ->
        intentHandler.handleIntent(ModifyGroupHarvestIntent.ChangeLocation(
                newLocation = value,
                locationChangedAfterUserInteraction = true,
        ))
    }

    override val booleanEventDispatcher = BooleanEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.NOT_EDIBLE -> ModifyGroupHarvestIntent.ChangeNotEdible(value)
            GroupHarvestField.ALONE -> ModifyGroupHarvestIntent.ChangeAlone(value)
            GroupHarvestField.ANTLERS_LOST -> ModifyGroupHarvestIntent.ChangeAntlersLost(value)
            else -> throw createUnexpectedEventException(fieldId, "Boolean", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val doubleEventDispatcher = DoubleEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.WEIGHT_ESTIMATED -> ModifyGroupHarvestIntent.ChangeWeightEstimated(value)
            GroupHarvestField.WEIGHT_MEASURED -> ModifyGroupHarvestIntent.ChangeWeightMeasured(value)
            else -> throw createUnexpectedEventException(fieldId, "Double?", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val intEventDispatcher = IntEventDispatcher<GroupHarvestField> { fieldId, value ->
        val intent = when (fieldId) {
            GroupHarvestField.ACTOR_HUNTER_NUMBER -> ModifyGroupHarvestIntent.ChangeActorHunterNumber(value)
            GroupHarvestField.ANTLERS_WIDTH -> ModifyGroupHarvestIntent.ChangeAntlersWidth(value)
            GroupHarvestField.ANTLER_POINTS_LEFT -> ModifyGroupHarvestIntent.ChangeAntlerPointsLeft(value)
            GroupHarvestField.ANTLER_POINTS_RIGHT -> ModifyGroupHarvestIntent.ChangeAntlerPointsRight(value)
            GroupHarvestField.ANTLERS_GIRTH -> ModifyGroupHarvestIntent.ChangeAntlersGirth(value)
            GroupHarvestField.ANTLER_SHAFT_WIDTH -> ModifyGroupHarvestIntent.ChangeAntlerShaftWidth(value)
            GroupHarvestField.ANTLERS_LENGTH -> ModifyGroupHarvestIntent.ChangeAntlersLength(value)
            GroupHarvestField.ANTLERS_INNER_WIDTH -> ModifyGroupHarvestIntent.ChangeAntlersInnerWidth(value)
            else -> throw createUnexpectedEventException(fieldId, "Int?", value)
        }

        intentHandler.handleIntent(intent)
    }

    private fun createUnexpectedEventException(
        fieldId: GroupHarvestField,
        eventType: String,
        newValue: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (newValue: $newValue)")
    }
}

