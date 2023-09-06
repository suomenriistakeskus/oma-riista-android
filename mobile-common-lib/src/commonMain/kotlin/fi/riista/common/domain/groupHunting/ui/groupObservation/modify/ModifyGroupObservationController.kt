package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.asGroupMember
import fi.riista.common.domain.groupHunting.model.asGuest
import fi.riista.common.domain.groupHunting.model.isMember
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationGeoLocationEventToIntentMapper
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationHuntingDayEventDispatcher
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationHuntingDayEventToIntentMapper
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationIntEventDispatcher
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationIntEventToIntentMapper
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationIntent
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationLocationEventDispatcher
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationStringWithIdEventDispatcher
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationStringWithIdEventToIntentMapper
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationTimeEventDispatcher
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationTimeEventToIntentMapper
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.logging.getLogger
import fi.riista.common.model.StringWithId
import fi.riista.common.model.changeDate
import fi.riista.common.model.changeTime
import fi.riista.common.reactive.Observable
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import fi.riista.common.ui.dataField.withRequirement
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.serialization.Serializable

abstract class ModifyGroupObservationController(
    val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ModifyGroupObservationViewModel>(),
    IntentHandler<GroupObservationIntent>,
    HasUnreproducibleState<ModifyGroupObservationController.SavedState> {

    val huntingDayEventDispatcher: GroupObservationHuntingDayEventDispatcher = GroupObservationHuntingDayEventToIntentMapper(intentHandler = this)
    val timeEventDispatcher: GroupObservationTimeEventDispatcher = GroupObservationTimeEventToIntentMapper(intentHandler = this)
    val locationEventDispatcher: GroupObservationLocationEventDispatcher = GroupObservationGeoLocationEventToIntentMapper(intentHandler = this)
    val intEventDispatcher: GroupObservationIntEventDispatcher = GroupObservationIntEventToIntentMapper(intentHandler = this)
    val stringWithIdEventDispatcher: GroupObservationStringWithIdEventDispatcher = GroupObservationStringWithIdEventToIntentMapper(intentHandler = this)

    private val fieldProducer = ModifyGroupHuntingObservationFieldProducer(stringProvider = stringProvider)

    protected var restoredObservation: GroupHuntingObservationData? = null

    /**
     * Can the observation location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the observation.
     */
    val observationLocationCanBeMovedAutomatically = Observable<Boolean?>(null)

    private val pendingIntents = mutableListOf<GroupObservationIntent>()

    override fun handleIntent(intent: GroupObservationIntent) {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(
                ViewModelLoadStatus.Loaded(
                viewModel = handleIntent(intent, viewModel)
            ))
        } else {
            pendingIntents.add(intent)
        }
    }

    private fun handleIntent(
        intent: GroupObservationIntent,
        viewModel: ModifyGroupObservationViewModel,
    ): ModifyGroupObservationViewModel {

        var newHuntingDays = viewModel.huntingDays
        val observation = viewModel.observation

        val updatedObservation = when (intent) {
            is GroupObservationIntent.ChangeTime -> {
                observation.copy(pointOfTime = observation.pointOfTime.changeTime(intent.newTime))
            }
            is GroupObservationIntent.ChangeHuntingDay -> {
                newHuntingDays = viewModel.huntingDays.ensureDayExists(intent.huntingDayId)

                updateHuntingDay(observation, intent.huntingDayId)
            }
            is GroupObservationIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    observationLocationCanBeMovedAutomatically.set(false)
                }
                observation.copy(geoLocation = intent.newLocation)
            }
            is GroupObservationIntent.ChangeActor -> {
                updateActor(observation, viewModel, intent.newActor)
            }
            is GroupObservationIntent.ChangeActorHunterNumber -> {
                updateActorHunterNumber(observation, intent.hunterNumber.toString())
            }
            is GroupObservationIntent.ChangeMooselikeMaleAmount -> {
                observation.copy(mooselikeMaleAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeFemaleAmount -> {
                observation.copy(mooselikeFemaleAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeCalfAmount -> {
                observation.copy(mooselikeCalfAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeFemale1CalfAmount -> {
                observation.copy(mooselikeFemale1CalfAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeFemale2CalfsAmount -> {
                observation.copy(mooselikeFemale2CalfsAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeFemale3CalfsAmount -> {
                observation.copy(mooselikeFemale3CalfsAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeFemale4CalfsAmount -> {
                observation.copy(mooselikeFemale4CalfsAmount = intent.newAmount)
            }
            is GroupObservationIntent.ChangeMooselikeUnknownSpecimenAmount -> {
                observation.copy(mooselikeUnknownSpecimenAmount = intent.newAmount)
            }
        }

        return createViewModel(
            observation = updatedObservation,
            huntingGroupMembers = viewModel.huntingGroupMembers,
            huntingDays = newHuntingDays,
            huntingGroupArea = viewModel.huntingGroupArea,
        )
    }

    protected fun createViewModel(
        observation: GroupHuntingObservationData,
        huntingGroupMembers: List<HuntingGroupMember>,
        huntingDays: List<GroupHuntingDay>,
        huntingGroupArea: HuntingGroupArea?
    ): ModifyGroupObservationViewModel {

        var fieldsToBeDisplayed = getFieldsToBeDisplayed(observation)
        val validationErrors = GroupHuntingObservationValidator.validate(observation, huntingDays, fieldsToBeDisplayed)
        val observationIsValid = validationErrors.isEmpty()
        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors)

        return ModifyGroupObservationViewModel(
            observation = observation,
            huntingGroupMembers = huntingGroupMembers,
            huntingDays = huntingDays,
            fields = fieldsToBeDisplayed.map { fieldSpecification ->
                fieldProducer.createField(fieldSpecification, observation, huntingGroupMembers)
            },
            observationIsValid = observationIsValid,
            huntingGroupArea = huntingGroupArea,
        )
    }

    private fun List<FieldSpecification<GroupObservationField>>.injectErrorLabels(
        validationErrors: List<GroupHuntingObservationValidator.Error>
    ): List<FieldSpecification<GroupObservationField>> {
        if (validationErrors.isEmpty()) {
            return this
        }

        val result = mutableListOf<FieldSpecification<GroupObservationField>>()
        forEach { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                GroupObservationField.HUNTING_DAY_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(GroupHuntingObservationValidator.Error.TIME_NOT_WITHIN_HUNTING_DAY)) {
                        result.add(GroupObservationField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY.noRequirement())
                    }
                }
                GroupObservationField.ACTOR_HUNTER_NUMBER,
                GroupObservationField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                GroupObservationField.SPECIES_CODE,
                GroupObservationField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                GroupObservationField.DATE_AND_TIME,
                GroupObservationField.LOCATION,
                GroupObservationField.OBSERVATION_TYPE,
                GroupObservationField.ACTOR,
                GroupObservationField.AUTHOR,
                GroupObservationField.HEADLINE_SPECIMEN_DETAILS,
                GroupObservationField.MOOSELIKE_MALE_AMOUNT,
                GroupObservationField.MOOSELIKE_FEMALE_AMOUNT,
                GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT,
                GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT,
                GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT,
                GroupObservationField.MOOSELIKE_FEMALE_4CALF_AMOUNT,
                GroupObservationField.MOOSELIKE_CALF_AMOUNT,
                GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT -> {
                    result.add(fieldSpecification)
                }
            }
        }
        return result
    }

    protected fun GroupHuntingObservationData.selectInitialHuntingDayForObservation(
        huntingDays: List<GroupHuntingDay>
    ): GroupHuntingObservationData {
        // automatic hunting day selection is relevant only for moose and only
        // if there's no hunting day.
        // - deer animals will get their hunting day when saving harvest
        // - don't replace previously set hunting day in any case
        if (!gameSpeciesCode.isMoose() || huntingDayId != null) {
            return this
        }

        val observationDate = pointOfTime.date

        return huntingDays.find { observationDate in it.startDateTime.date .. it.endDateTime.date }
            ?.let { huntingDay ->
                copy(huntingDayId = huntingDay.id)
            }
                ?: this
    }

    /**
     * Tries to make sure that the list contains a [GroupHuntingDay] that has the
     * given [huntingDayId].
     *
     * Tries to find a hunting day with the [huntingDayId] if the list currently does
     * not contain a matching hunting day.
     *
     * This function should be called when hunting day is changed. It is possible
     * that new hunting day has been added and that hunting day is not among the hunting
     * days in the viewmodel.
     */
    private fun List<GroupHuntingDay>.ensureDayExists(
        huntingDayId: GroupHuntingDayId
    ): List<GroupHuntingDay> {
        return if (find { it.id == huntingDayId } != null) {
            this
        } else {
            findHuntingDay(huntingDayId)
                ?.let { this + it }
                ?: this
        }
    }

    private fun updateHuntingDay(observation: GroupHuntingObservationData, huntingDayId: GroupHuntingDayId): GroupHuntingObservationData {
        val huntingDay = findHuntingDay(huntingDayId)
        return if (huntingDay != null) {
            logger.v { "Hunting day exists, changing hunting day to $huntingDayId" }

            // update harvest point of time to match. Don't alter timestamp.
            val pointOfTime = observation.pointOfTime.changeDate(
                newDate = huntingDay.startDateTime.date
            )

            observation.copy(
                huntingDayId = huntingDayId,
                pointOfTime = pointOfTime,
            )
        } else {
            logger.v { "New hunting day id $huntingDayId but matching day was not found" }
            observation
        }
    }

    protected abstract fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay?

    private fun updateActor(
        observation: GroupHuntingObservationData,
        viewModel: ModifyGroupObservationViewModel,
        newActor: StringWithId
    ): GroupHuntingObservationData {
        logger.v { "New actor $newActor" }
        return if (newActor.id != GroupHuntingPerson.SearchingByHunterNumber.ID) {
            val member = viewModel.huntingGroupMembers.firstOrNull { it.personId == newActor.id }
            if (member?.hunterNumber != null) {
                val person = PersonWithHunterNumber(
                    id = member.personId,
                    rev = 0,
                    // byName != firstName, but we don't first name, so use what we have.
                    // Names are not updated when actor is saved to backend so this shouldn't be a big issue.
                    byName = member.firstName,
                    lastName = member.lastName,
                    hunterNumber = member.hunterNumber,
                    extendedName = null)
                observation.copy(actorInfo = person.asGroupMember())
            } else {
                logger.e { "Selected actor not found from list of group members! Not updating" }
                observation
            }
        } else {
            when (viewModel.observation.actorInfo) {
                is GroupHuntingPerson.GroupMember,
                is GroupHuntingPerson.Guest,
                GroupHuntingPerson.Unknown -> {
                    observation.copy(actorInfo = GroupHuntingPerson.SearchingByHunterNumber.startSearch())
                }
                is GroupHuntingPerson.SearchingByHunterNumber -> observation
            }
        }
    }

    private fun updateActorHunterNumber(
        observation: GroupHuntingObservationData,
        hunterNumber: HunterNumber,
    ): GroupHuntingObservationData {
        return (observation.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber)
            ?.let { currentSearch ->
                val updatedSearch = currentSearch.withUpdatedHunterNumber(hunterNumber)
                if (updatedSearch.status == GroupHuntingPerson.SearchingByHunterNumber.Status.VALID_HUNTER_NUMBER_ENTERED) {
                    // we've got a valid hunter number -> initiate search
                    updateViewModelSuspended {
                        searchActorByHunterNumber()
                    }
                }
                observation.copy(actorInfo = updatedSearch)
            }
            ?: observation
    }

    private fun getFieldsToBeDisplayed(
        observation: GroupHuntingObservationData,
    ): List<FieldSpecification<GroupObservationField>> {

        return FieldSpecificationListBuilder<GroupObservationField>()
            .add(
                GroupObservationField.LOCATION.required(),
                GroupObservationField.SPECIES_CODE.required(),
                GroupObservationField.HUNTING_DAY_AND_TIME.required(),
                GroupObservationField.OBSERVATION_TYPE.noRequirement(),
                GroupObservationField.ACTOR.required(),
                GroupObservationField.ACTOR_HUNTER_NUMBER
                    .withRequirement {
                        if (observation.actorInfo is GroupHuntingPerson.SearchingByHunterNumber) {
                            FieldRequirement.required()
                        } else {
                            FieldRequirement.voluntary()
                        }
                    }
                    .takeIf {
                        observation.actorInfo !is GroupHuntingPerson.Unknown
                    },
                GroupObservationField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary().takeIf {
                    observation.actorInfo is GroupHuntingPerson.SearchingByHunterNumber
                },
                GroupObservationField.HEADLINE_SPECIMEN_DETAILS.noRequirement(),
                GroupObservationField.MOOSELIKE_MALE_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_FEMALE_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_CALF_AMOUNT.required(),
                GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT.required(),
            )
            .toList()
    }

    protected fun ModifyGroupObservationViewModel.applyPendingIntents(): ModifyGroupObservationViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    private suspend fun searchActorByHunterNumber() {
        var viewModel = getLoadedViewModelOrNull()
            ?: kotlin.run {
                logger.d { "Cannot search actor by hunter number. No loaded viewmodel!" }
                return
            }

        val initialHunterNumberSearch = viewModel.observation.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber
        val hunterNumber = initialHunterNumberSearch?.hunterNumber
            ?: kotlin.run {
                logger.d { "No hunter number, cannot search actor." }
                return
            }

        val searchingHunterNumber = initialHunterNumberSearch.copy(
            status = GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER
        )

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = createViewModel(
                observation = viewModel.observation.copy(actorInfo = searchingHunterNumber),
                huntingGroupMembers = viewModel.huntingGroupMembers,
                huntingDays = viewModel.huntingDays,
                huntingGroupArea = viewModel.huntingGroupArea,
            )
        ))

        val hunter = searchPersonByHunterNumber(hunterNumber)

        viewModel = getLoadedViewModelOrNull()
            ?: kotlin.run {
                logger.d { "Cannot search actor by hunter number. No loaded viewmodel!" }
                return
            }

        // only update viewModel if user has not updated viewModel while we we're fetching
        // hunter information
        if (searchingHunterNumber != viewModel.observation.actorInfo) {
            logger.d { "User updated actor selection while fetching hunter, discarding result" }
            return
        }

        val actorInfo = if (hunter != null) {
            logger.d { "Found hunter ${hunter.byName} ${hunter.lastName}" }
            if (viewModel.huntingGroupMembers.isMember(hunter)) {
                hunter.asGroupMember()
            } else {
                hunter.asGuest()
            }
        } else {
            logger.d { "Couldn't find hunter for hunterNumber = $hunterNumber" }
            searchingHunterNumber.copy(status = GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED)
        }

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = createViewModel(
                observation = viewModel.observation.copy(actorInfo = actorInfo),
                huntingGroupMembers = viewModel.huntingGroupMembers,
                huntingDays = viewModel.huntingDays,
                huntingGroupArea = viewModel.huntingGroupArea
            )
        ))
    }

    protected abstract suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumber?

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.observation?.let {
            SavedState(observationData = it, observationLocationCanBeMovedAutomatically.value)
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredObservation = state.observationData
        observationLocationCanBeMovedAutomatically.set(state.observationLocationCanBeMovedAutomatically)
    }

    @Serializable
    data class SavedState(
        val observationData: GroupHuntingObservationData,
        val observationLocationCanBeMovedAutomatically: Boolean?,
    )

    companion object {
        private val logger by getLogger(ModifyGroupObservationController::class)
    }
}
