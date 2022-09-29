package fi.riista.common.domain.groupHunting.ui.groupObservation

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.logging.getLogger
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.intent.IntentHandler

internal class GroupObservationTimeEventToIntentMapper(
    private val intentHandler: IntentHandler<GroupObservationIntent>
) : GroupObservationTimeEventDispatcher {

    override fun dispatchLocalTimeChanged(fieldId: GroupObservationField, value: LocalTime) {
        val intent = when (fieldId) {
            GroupObservationField.HUNTING_DAY_AND_TIME -> GroupObservationIntent.ChangeTime(value)
            else -> {
                logger.w { "GroupObservationField $fieldId has unexpected LocalTime value of $value" }
                return
            }
        }

        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(GroupObservationTimeEventToIntentMapper::class)
    }
}

internal class GroupObservationHuntingDayEventToIntentMapper(
    private val intentHandler: IntentHandler<GroupObservationIntent>
) : GroupObservationHuntingDayEventDispatcher {

    override fun dispatchHuntingDayChanged(fieldId: GroupObservationField, value: GroupHuntingDayId) {
        val intent = when (fieldId) {
            GroupObservationField.HUNTING_DAY_AND_TIME -> GroupObservationIntent.ChangeHuntingDay(value)
            else -> {
                logger.w { "GroupObservationField $fieldId has unexpected GroupHuntingDayId value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(GroupObservationHuntingDayEventToIntentMapper::class)
    }
}

internal class GroupObservationStringWithIdEventToIntentMapper(
    private val intentHandler: IntentHandler<GroupObservationIntent>
) : GroupObservationStringWithIdEventDispatcher {

    override fun dispatchStringWithIdChanged(fieldId: GroupObservationField, value: List<StringWithId>) {
        if (value.size != 1) {
            throw RuntimeException("Wrong number of values for field $fieldId (newValue: $value)")
        }
        val intent = when (fieldId) {
            GroupObservationField.ACTOR -> GroupObservationIntent.ChangeActor(value[0])
            else -> {
                logger.w { "GroupObservationField $fieldId has unexpected String value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(GroupObservationStringWithIdEventToIntentMapper::class)
    }
}

internal class GroupObservationGeoLocationEventToIntentMapper(
    private val intentHandler: IntentHandler<GroupObservationIntent>
) : GroupObservationLocationEventDispatcher {

    override fun dispatchLocationChanged(fieldId: GroupObservationField, value: ETRMSGeoLocation) {
        intentHandler.handleIntent(GroupObservationIntent.ChangeLocation(
            newLocation = value,
            locationChangedAfterUserInteraction = true,
        ))
    }
}

internal class GroupObservationIntEventToIntentMapper(
    private val intentHandler: IntentHandler<GroupObservationIntent>
) : GroupObservationIntEventDispatcher {

    override fun dispatchIntChanged(fieldId: GroupObservationField, value: Int?) {
        val intent = when (fieldId) {
            GroupObservationField.ACTOR_HUNTER_NUMBER -> GroupObservationIntent.ChangeActorHunterNumber(value)
            GroupObservationField.MOOSELIKE_MALE_AMOUNT -> GroupObservationIntent.ChangeMooselikeMaleAmount(value)
            GroupObservationField.MOOSELIKE_FEMALE_AMOUNT -> GroupObservationIntent.ChangeMooselikeFemaleAmount(value)
            GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT -> GroupObservationIntent.ChangeMooselikeFemale1CalfAmount(value)
            GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT -> GroupObservationIntent.ChangeMooselikeFemale2CalfsAmount(value)
            GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT -> GroupObservationIntent.ChangeMooselikeFemale3CalfsAmount(value)
            GroupObservationField.MOOSELIKE_FEMALE_4CALF_AMOUNT -> GroupObservationIntent.ChangeMooselikeFemale4CalfsAmount(value)
            GroupObservationField.MOOSELIKE_CALF_AMOUNT -> GroupObservationIntent.ChangeMooselikeCalfAmount(value)
            GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT -> GroupObservationIntent.ChangeMooselikeUnknownSpecimenAmount(value)
            else -> {
                logger.w { "GroupObservationField $fieldId has unexpected Int value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(GroupObservationIntEventToIntentMapper::class)
    }
}
