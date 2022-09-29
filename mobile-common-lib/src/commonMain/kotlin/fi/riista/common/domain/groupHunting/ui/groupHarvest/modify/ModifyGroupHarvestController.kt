package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.groupHunting.ui.groupHarvest.GroupHuntingHarvestFields
import fi.riista.common.domain.model.HarvestSpecimen
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.changeDate
import fi.riista.common.model.changeTime
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.contains
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.serialization.Serializable

/**
 * A controller for modifying [GroupHuntingHarvest] information
 */
abstract class ModifyGroupHarvestController(
    protected val groupHuntingContext: GroupHuntingContext,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ModifyGroupHarvestViewModel>(),
    IntentHandler<ModifyGroupHarvestIntent>,
    HasUnreproducibleState<ModifyGroupHarvestController.SavedState> {

    val eventDispatchers: ModifyGroupHarvestEventDispatcher by lazy {
        ModifyGroupHarvestEventToIntentMapper(intentHandler = this)
    }

    protected var restoredHarvestData: GroupHuntingHarvestData? = null

    /**
     * Can the harvest location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the harvest.
     */
    protected var harvestLocationCanBeUpdatedAutomatically: Boolean = true

    private val pendingIntents = mutableListOf<ModifyGroupHarvestIntent>()

    private val fieldProducer = EditGroupHuntingHarvestFieldProducer(stringProvider = stringProvider)

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    override fun handleIntent(intent: ModifyGroupHarvestIntent) {
        // It is possible that intent is sent already before we have Loaded viewmodel.
        // This is the case e.g. when location is updated in external activity (on android)
        // and the activity/fragment utilizing this controller was destroyed. In that case
        // the call cycle could be:
        // - finish map activity with result
        // - create fragment / activity (that will utilize this controller)
        // - restore controller state
        // - handle activity result (e.g. dispatch location updated event)
        // - resume -> loadViewModel
        //
        // tackle the above situation by collecting intents to pendingIntents and restored
        // viewmodel with those when viewModel has been loaded
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(ViewModelLoadStatus.Loaded(
                    viewModel = handleIntent(intent, viewModel)
            ))
        } else {
            pendingIntents.add(intent)
        }
    }

    private fun handleIntent(
        intent: ModifyGroupHarvestIntent,
        viewModel: ModifyGroupHarvestViewModel,
    ): ModifyGroupHarvestViewModel {

        var newHuntingDays = viewModel.huntingDays
        val harvest = viewModel.harvest
        val updatedHarvest = when (intent) {
            is ModifyGroupHarvestIntent.ChangeAdditionalInformation -> {
                logger.v { "New additional info ${intent.newAdditionalInformation}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        additionalInfo = intent.newAdditionalInformation
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeGender -> {
                logger.v { "New gender ${intent.newGender}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        gender = BackendEnum.create(intent.newGender)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeDateAndTime -> {
                logger.v { "New date and time ${intent.newDateAndTime}" }
                harvest.copy(pointOfTime = intent.newDateAndTime)
            }
            is ModifyGroupHarvestIntent.ChangeTime -> {
                logger.v { "New time ${intent.newTime}" }
                harvest.copy(pointOfTime = harvest.pointOfTime.changeTime(intent.newTime))
            }
            is ModifyGroupHarvestIntent.ChangeHuntingDay -> {
                newHuntingDays = viewModel.huntingDays.ensureDayExists(intent.huntingDayId)

                updateHuntingDay(harvest, intent.huntingDayId, newHuntingDays)
            }
            is ModifyGroupHarvestIntent.ChangeDeerHuntingType -> {
                harvest.copy(deerHuntingType = intent.deerHuntingType)
            }
            is ModifyGroupHarvestIntent.ChangeDeerHuntingOtherTypeDescription -> {
                harvest.copy(deerHuntingOtherTypeDescription = intent.deerHuntingOtherTypeDescription)
            }
            is ModifyGroupHarvestIntent.ChangeAge -> {
                logger.v { "New age ${intent.newAge}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        age = BackendEnum.create(intent.newAge)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeLocation -> {
                logger.v { "New location ${intent.newLocation}, " +
                        "updated after interaction = ${intent.locationChangedAfterUserInteraction}" }
                if (intent.locationChangedAfterUserInteraction) {
                    harvestLocationCanBeUpdatedAutomatically = false
                }

                harvest.copy(geoLocation = intent.newLocation)
            }
            is ModifyGroupHarvestIntent.ChangeNotEdible -> {
                logger.v { "New notEdible ${intent.newNotEdible}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        notEdible = intent.newNotEdible
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeWeightEstimated -> {
                logger.v { "New weight estimated ${intent.newWeight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        weightEstimated = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeWeightMeasured -> {
                logger.v { "New weight measured ${intent.newWeight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        weightMeasured = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeFitnessClass -> {
                logger.v { "New fitness class ${intent.newFitnessClass}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        fitnessClass = intent.newFitnessClass
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersType -> {
                logger.v { "New antlers type ${intent.newAntlersType}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersType = intent.newAntlersType
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersWidth -> {
                logger.v { "New antlers width ${intent.newAntlersWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersWidth = intent.newAntlersWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlerPointsLeft -> {
                logger.v { "New antler points left ${intent.newAntlerPointsLeft}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerPointsLeft = intent.newAntlerPointsLeft
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlerPointsRight -> {
                logger.v { "New antler points right ${intent.newAntlerPointsRight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerPointsRight = intent.newAntlerPointsRight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersLost -> {
                logger.v { "New antlers lost ${intent.newAntlersLost}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersLost = intent.newAntlersLost
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersGirth -> {
                logger.v { "New antlers girth ${intent.newAntlersGirth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersGirth = intent.newAntlersGirth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlerShaftWidth -> {
                logger.v { "New antler shaft width ${intent.newAntlerShaftWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerShaftWidth = intent.newAntlerShaftWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersLength -> {
                logger.v { "New antlers length ${intent.newAntlersLength}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersLength = intent.newAntlersLength
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAntlersInnerWidth -> {
                logger.v { "New antlers inner width ${intent.newAntlersInnerWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersInnerWidth = intent.newAntlersInnerWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeAlone -> {
                logger.v { "New alone ${intent.newAlone}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(alone = intent.newAlone)
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyGroupHarvestIntent.ChangeActor -> {
                logger.v { "New actor ${intent.newActor}" }
                if (intent.newActor.id != GroupHuntingPerson.SearchingByHunterNumber.ID) {
                    val member = viewModel.huntingGroupMembers.firstOrNull { it.personId == intent.newActor.id }
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
                        harvest.copy(actorInfo = person.asGroupMember())
                    } else {
                        logger.e { "Selected actor not found from list of group members! Not updating" }
                        harvest
                    }
                } else {
                    when (viewModel.harvest.actorInfo) {
                        is GroupHuntingPerson.GroupMember,
                        is GroupHuntingPerson.Guest,
                        GroupHuntingPerson.Unknown -> {
                            harvest.copy(actorInfo = GroupHuntingPerson.SearchingByHunterNumber.startSearch())
                        }
                        is GroupHuntingPerson.SearchingByHunterNumber -> harvest
                    }
                }
            }
            is ModifyGroupHarvestIntent.ChangeActorHunterNumber -> {
                (viewModel.harvest.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber)
                    ?.let { currentSearch ->
                        val updatedSearch = currentSearch.withUpdatedHunterNumber(intent.hunterNumber.toString())
                        if (updatedSearch.status == GroupHuntingPerson.SearchingByHunterNumber.Status.VALID_HUNTER_NUMBER_ENTERED) {
                            // we've got a valid hunter number -> initiate search
                            updateViewModelSuspended {
                                searchActorByHunterNumber()
                            }
                        }
                        harvest.copy(actorInfo = updatedSearch)
                    }
                    ?: harvest
            }
        }

        return createViewModel(
                harvest = updatedHarvest,
                huntingGroupStatus = viewModel.huntingGroupStatus,
                huntingGroupMembers = viewModel.huntingGroupMembers,
                huntingGroupPermit = viewModel.huntingGroupPermit,
                huntingDays = newHuntingDays,
                huntingGroupArea = viewModel.huntingGroupArea,
        )
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

    /**
     * Updates the harvest hunting day if the given [huntingDays] list contains a
     * hunting day with given [huntingDayId].
     */
    private fun updateHuntingDay(
        harvest: GroupHuntingHarvestData,
        huntingDayId: GroupHuntingDayId,
        huntingDays: List<GroupHuntingDay>,
    ): GroupHuntingHarvestData {
        val huntingDay = huntingDays.find { it.id == huntingDayId }
        return if (huntingDay != null) {
            logger.v { "Hunting day exists, changing hunting day to $huntingDayId" }

            // update harvest point of time to match. Don't alter timestamp.
            val pointOfTime = harvest.pointOfTime.changeDate(
                    newDate = huntingDay.startDateTime.date
            )

            harvest.copy(
                    huntingDayId = huntingDayId,
                    pointOfTime = pointOfTime,
            )
        } else {
            logger.v { "New hunting day id $huntingDayId but matching day was not found" }
            harvest
        }
    }

    protected abstract fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay?

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.harvest?.let {
            SavedState(
                    harvestData = it,
                    harvestLocationCanBeUpdatedAutomatically = harvestLocationCanBeUpdatedAutomatically
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredHarvestData = state.harvestData
        harvestLocationCanBeUpdatedAutomatically = state.harvestLocationCanBeUpdatedAutomatically
    }

    private fun getFirstSpecimenOrCreate(harvest: GroupHuntingHarvestData) : HarvestSpecimen {
        return harvest.specimens.firstOrNull()
                ?: HarvestSpecimen().ensureDefaultValuesAreSet()
    }

    private fun updateFirstSpecimen(harvest: GroupHuntingHarvestData, specimen: HarvestSpecimen): GroupHuntingHarvestData {
        return harvest.copy(
                specimens = listOf(specimen) + harvest.specimens.drop(1)
        )
    }

    protected fun createViewModel(
        harvest: GroupHuntingHarvestData,
        huntingGroupStatus: HuntingGroupStatus,
        huntingGroupMembers: List<HuntingGroupMember>,
        huntingGroupPermit: HuntingGroupPermit,
        huntingDays: List<GroupHuntingDay>,
        huntingGroupArea: HuntingGroupArea?,
    ): ModifyGroupHarvestViewModel {

        var fieldsToBeDisplayed = GroupHuntingHarvestFields.getFieldsToBeDisplayed(
                GroupHuntingHarvestFields.Context(
                        harvest = harvest,
                        mode = GroupHuntingHarvestFields.Context.Mode.EDIT
                )
        )

        val validationErrors = GroupHuntingHarvestValidator.validate(
                harvest, huntingDays, huntingGroupPermit, fieldsToBeDisplayed)

        val harvestIsValid = validationErrors.isEmpty()

        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors)

        return ModifyGroupHarvestViewModel(
                harvest = harvest,
                huntingGroupStatus = huntingGroupStatus,
                huntingGroupMembers = huntingGroupMembers,
                huntingGroupPermit = huntingGroupPermit,
                huntingDays = huntingDays,
                fields = fieldsToBeDisplayed.map { fieldSpecification ->
                    fieldProducer.createField(fieldSpecification, harvest, huntingGroupMembers)
                },
                harvestIsValid = harvestIsValid,
                huntingGroupArea = huntingGroupArea,
        )
    }

    private fun List<FieldSpecification<GroupHarvestField>>.injectErrorLabels(
        validationErrors: List<GroupHuntingHarvestValidator.Error>
    ): List<FieldSpecification<GroupHarvestField>> {
        if (validationErrors.isEmpty()) {
            return this
        }

        val result = mutableListOf<FieldSpecification<GroupHarvestField>>()

        forEach { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                GroupHarvestField.HUNTING_DAY_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(GroupHuntingHarvestValidator.Error.TIME_NOT_WITHIN_HUNTING_DAY)) {
                        result.add(GroupHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY.noRequirement())
                    }
                }
                GroupHarvestField.DATE_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(GroupHuntingHarvestValidator.Error.DATE_NOT_WITHIN_GROUP_PERMIT)) {
                        result.add(GroupHarvestField.ERROR_DATE_NOT_WITHIN_GROUP_PERMIT.noRequirement())
                    }
                }
                GroupHarvestField.SPECIES_CODE,
                GroupHarvestField.ERROR_DATE_NOT_WITHIN_GROUP_PERMIT,
                GroupHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                GroupHarvestField.LOCATION,
                GroupHarvestField.DEER_HUNTING_TYPE,
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
                GroupHarvestField.ACTOR_HUNTER_NUMBER,
                GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                GroupHarvestField.GENDER,
                GroupHarvestField.AGE,
                GroupHarvestField.NOT_EDIBLE,
                GroupHarvestField.ADDITIONAL_INFORMATION,
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
                GroupHarvestField.HEADLINE_SHOOTER,
                GroupHarvestField.HEADLINE_SPECIMEN,
                GroupHarvestField.WEIGHT,
                GroupHarvestField.WEIGHT_ESTIMATED,
                GroupHarvestField.WEIGHT_MEASURED,
                GroupHarvestField.FITNESS_CLASS,
                GroupHarvestField.ANTLER_INSTRUCTIONS,
                GroupHarvestField.ANTLERS_TYPE,
                GroupHarvestField.ANTLERS_WIDTH,
                GroupHarvestField.ANTLER_POINTS_LEFT,
                GroupHarvestField.ANTLER_POINTS_RIGHT,
                GroupHarvestField.ANTLERS_LOST,
                GroupHarvestField.ANTLERS_GIRTH,
                GroupHarvestField.ANTLER_SHAFT_WIDTH,
                GroupHarvestField.ANTLERS_LENGTH,
                GroupHarvestField.ANTLERS_INNER_WIDTH,
                GroupHarvestField.ACTOR,
                GroupHarvestField.AUTHOR,
                GroupHarvestField.ALONE -> {
                    result.add(fieldSpecification)
                }
            }
        }
        return result
    }

    protected fun ModifyGroupHarvestViewModel.applyPendingIntents(): ModifyGroupHarvestViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    protected fun ModifyGroupHarvestViewModel.getValidatedHarvestDataOrNull(): GroupHuntingHarvestData? {
        val displayedFields = GroupHuntingHarvestFields.getFieldsToBeDisplayed(
                GroupHuntingHarvestFields.Context(
                        harvest = harvest,
                        mode = GroupHuntingHarvestFields.Context.Mode.EDIT
                )
        )

        val validationErrors = GroupHuntingHarvestValidator.validate(
                harvest,
                huntingDays,
                huntingGroupPermit,
                displayedFields
        )
        if (validationErrors.isNotEmpty()) {
            return null
        }

        return harvest.createCopyWithFields(displayedFields)

    }

    protected fun GroupHuntingHarvestData.ensureDefaultValuesAreSet(): GroupHuntingHarvestData {
        return copy(
                specimens = specimens.map { it.ensureDefaultValuesAreSet() }
        )
    }

    protected fun HarvestSpecimen.ensureDefaultValuesAreSet(): HarvestSpecimen {
        return copy(
                notEdible = notEdible ?: false,
                alone = alone ?: false,
                antlersLost = antlersLost ?: false
        )
    }

    protected fun GroupHuntingHarvestData.selectInitialHuntingDayForHarvest(
        huntingDays: List<GroupHuntingDay>
    ): GroupHuntingHarvestData {
        // automatic hunting day selection is relevant only for moose and only
        // if there's no hunting day.
        // - deer animals will get their hunting day when saving harvest
        // - don't replace previously set hunting day in any case
        if (!gameSpeciesCode.isMoose() || huntingDayId != null) {
            return this
        }

        val harvestDate = pointOfTime.date

        return huntingDays.find { harvestDate in it.startDateTime.date .. it.endDateTime.date }
            ?.let { huntingDay ->
                copy(huntingDayId = huntingDay.id)
            }
                ?: this
    }

    fun shouldCreateObservation(): Boolean {
        return getLoadedViewModelOrNull()?.harvest?.gameSpeciesCode?.isMoose() == true
    }

    private fun GroupHuntingHarvestData.createCopyWithFields(
        fields: List<FieldSpecification<GroupHarvestField>>,
    ): GroupHuntingHarvestData {
        return copy(
                // copy deer hunting type even though its requirement status most likely wont change
                // - species code cannot be changed and thus deer hunting type visibility should
                //   remain unchanged
                deerHuntingType = deerHuntingType
                    .takeIf { fields.contains(GroupHarvestField.DEER_HUNTING_TYPE) }
                        ?: BackendEnum.create(null),
                deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription
                    .takeIf { fields.contains(GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION) },
                specimens = specimens.createCopyWithFields(fields),
        )
    }

    private fun List<HarvestSpecimen>.createCopyWithFields(
        fields: List<FieldSpecification<GroupHarvestField>>,
    ): List<HarvestSpecimen> {
        // fields are determined based on the first specimen i.e. only create copy of the first specimen.
        // Let rest of the specimen be what they currently are (there shouldn't be any though)
        val specimen = first()

        val firstSpecimen = HarvestSpecimen(
                id = specimen.id,
                rev = specimen.rev,
                gender = specimen.gender.takeIf { fields.contains(GroupHarvestField.GENDER) },
                age = specimen.age.takeIf { fields.contains(GroupHarvestField.AGE) },
                weight = specimen.weight.takeIf { fields.contains(GroupHarvestField.WEIGHT) },
                weightEstimated = specimen.weightEstimated.takeIf { fields.contains(GroupHarvestField.WEIGHT_ESTIMATED) },
                weightMeasured = specimen.weightMeasured.takeIf { fields.contains(GroupHarvestField.WEIGHT_MEASURED) },
                fitnessClass = specimen.fitnessClass.takeIf { fields.contains(GroupHarvestField.FITNESS_CLASS) },
                antlersLost = specimen.antlersLost.takeIf { fields.contains(GroupHarvestField.ANTLERS_LOST) },
                antlersType = specimen.antlersType.takeIf { fields.contains(GroupHarvestField.ANTLERS_TYPE) },
                antlersWidth = specimen.antlersWidth.takeIf { fields.contains(GroupHarvestField.ANTLERS_WIDTH) },
                antlerPointsLeft = specimen.antlerPointsLeft.takeIf { fields.contains(GroupHarvestField.ANTLER_POINTS_LEFT) },
                antlerPointsRight = specimen.antlerPointsRight.takeIf { fields.contains(GroupHarvestField.ANTLER_POINTS_RIGHT) },
                antlersGirth = specimen.antlersGirth.takeIf { fields.contains(GroupHarvestField.ANTLERS_GIRTH) },
                antlersLength = specimen.antlersLength.takeIf { fields.contains(GroupHarvestField.ANTLERS_LENGTH) },
                antlersInnerWidth = specimen.antlersInnerWidth.takeIf { fields.contains(GroupHarvestField.ANTLERS_INNER_WIDTH) },
                antlerShaftWidth = specimen.antlerShaftWidth.takeIf { fields.contains(GroupHarvestField.ANTLER_SHAFT_WIDTH) },
                notEdible = specimen.notEdible.takeIf { fields.contains(GroupHarvestField.NOT_EDIBLE) },
                alone = specimen.alone.takeIf { fields.contains(GroupHarvestField.ALONE) },
                additionalInfo = specimen.additionalInfo.takeIf { fields.contains(GroupHarvestField.ADDITIONAL_INFORMATION) }
        )

        return listOf(firstSpecimen) + drop(1)
    }

    @Serializable
    data class SavedState(
        val harvestData: GroupHuntingHarvestData,
        val harvestLocationCanBeUpdatedAutomatically: Boolean,
    )

    private suspend fun searchActorByHunterNumber() {
        var viewModel = getLoadedViewModelOrNull()
            ?: kotlin.run {
                logger.d { "Cannot search actor by hunter number. No loaded viewmodel!" }
                return
            }

        val initialHunterNumberSearch = viewModel.harvest.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber
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
                harvest = viewModel.harvest.copy(actorInfo = searchingHunterNumber),
                huntingGroupStatus = viewModel.huntingGroupStatus,
                huntingGroupMembers = viewModel.huntingGroupMembers,
                huntingGroupPermit = viewModel.huntingGroupPermit,
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
        if (searchingHunterNumber != viewModel.harvest.actorInfo) {
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
                harvest = viewModel.harvest.copy(actorInfo = actorInfo),
                huntingGroupStatus = viewModel.huntingGroupStatus,
                huntingGroupMembers = viewModel.huntingGroupMembers,
                huntingGroupPermit = viewModel.huntingGroupPermit,
                huntingDays = viewModel.huntingDays,
                huntingGroupArea = viewModel.huntingGroupArea,
            )
        ))
    }

    private suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumber? {
        return groupHuntingContext.searchPersonByHunterNumber(hunterNumber)?.toPersonWithHunterNumber()
    }

    companion object {
        private val logger by getLogger(ModifyGroupHarvestController::class)
    }
}

