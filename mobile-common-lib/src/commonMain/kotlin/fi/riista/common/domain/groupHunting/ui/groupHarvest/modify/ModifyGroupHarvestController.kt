package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.domain.groupHunting.model.HuntingGroupStatus
import fi.riista.common.domain.groupHunting.model.asGroupMember
import fi.riista.common.domain.groupHunting.model.asGuest
import fi.riista.common.domain.groupHunting.model.isMember
import fi.riista.common.domain.groupHunting.ui.groupHarvest.GroupHuntingHarvestFields
import fi.riista.common.domain.groupHunting.ui.groupHarvest.validation.GroupHarvestValidator
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestEventDispatcher
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestEventToIntentMapper
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestIntent
import fi.riista.common.domain.harvest.validation.CommonHarvestValidator
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.isMoose
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
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.serialization.Serializable

/**
 * A controller for modifying [GroupHuntingHarvest] information
 */
abstract class ModifyGroupHarvestController(
    protected val groupHuntingContext: GroupHuntingContext,
    protected val localDateTimeProvider: LocalDateTimeProvider,
    speciesResolver: SpeciesResolver,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ModifyGroupHarvestViewModel>(),
    IntentHandler<ModifyHarvestIntent>,
    HasUnreproducibleState<ModifyGroupHarvestController.SavedState> {

    val eventDispatchers: ModifyHarvestEventDispatcher by lazy {
        ModifyHarvestEventToIntentMapper(intentHandler = this)
    }

    internal var restoredHarvestData: CommonHarvestData? = null

    /**
     * Can the harvest location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the harvest.
     */
    protected var harvestLocationCanBeUpdatedAutomatically: Boolean = true

    private val pendingIntents = mutableListOf<ModifyHarvestIntent>()

    private val fieldProducer = EditGroupHuntingHarvestFieldProducer(
        stringProvider = stringProvider,
        currentDateTimeProvider = localDateTimeProvider,
    )

    private val harvestValidator = GroupHarvestValidator(localDateTimeProvider, speciesResolver)

    override fun handleIntent(intent: ModifyHarvestIntent) {
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
        intent: ModifyHarvestIntent,
        viewModel: ModifyGroupHarvestViewModel,
    ): ModifyGroupHarvestViewModel {

        var newHuntingDays = viewModel.huntingDays
        val harvest = viewModel.harvest
        val updatedHarvest = when (intent) {
            is ModifyHarvestIntent.ChangeAdditionalInformation -> {
                logger.v { "New additional info ${intent.newAdditionalInformation}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        additionalInfo = intent.newAdditionalInformation
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeGender -> {
                logger.v { "New gender ${intent.newGender}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        gender = BackendEnum.create(intent.newGender)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeDateAndTime -> {
                logger.v { "New date and time ${intent.newDateAndTime}" }
                harvest.copy(pointOfTime = intent.newDateAndTime)
            }
            is ModifyHarvestIntent.ChangeTime -> {
                logger.v { "New time ${intent.newTime}" }
                harvest.copy(pointOfTime = harvest.pointOfTime.changeTime(intent.newTime))
            }
            is ModifyHarvestIntent.ChangeHuntingDay -> {
                newHuntingDays = viewModel.huntingDays.ensureDayExists(intent.huntingDayId)

                updateHuntingDay(harvest, intent.huntingDayId, newHuntingDays)
            }
            is ModifyHarvestIntent.ChangeDeerHuntingType -> {
                harvest.copy(deerHuntingType = intent.deerHuntingType)
            }
            is ModifyHarvestIntent.ChangeDeerHuntingOtherTypeDescription -> {
                harvest.copy(deerHuntingOtherTypeDescription = intent.deerHuntingOtherTypeDescription)
            }
            is ModifyHarvestIntent.ChangeAge -> {
                logger.v { "New age ${intent.newAge}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        age = BackendEnum.create(intent.newAge)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeLocation -> {
                logger.v { "New location ${intent.newLocation}, " +
                        "updated after interaction = ${intent.locationChangedAfterUserInteraction}" }
                if (intent.locationChangedAfterUserInteraction) {
                    harvestLocationCanBeUpdatedAutomatically = false
                }

                harvest.copy(location = intent.newLocation.asKnownLocation())
            }
            is ModifyHarvestIntent.ChangeNotEdible -> {
                logger.v { "New notEdible ${intent.newNotEdible}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        notEdible = intent.newNotEdible
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWeightEstimated -> {
                logger.v { "New weight estimated ${intent.newWeight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        weightEstimated = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWeightMeasured -> {
                logger.v { "New weight measured ${intent.newWeight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        weightMeasured = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeFitnessClass -> {
                logger.v { "New fitness class ${intent.newFitnessClass}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        fitnessClass = intent.newFitnessClass
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersType -> {
                logger.v { "New antlers type ${intent.newAntlersType}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersType = intent.newAntlersType
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersWidth -> {
                logger.v { "New antlers width ${intent.newAntlersWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersWidth = intent.newAntlersWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerPointsLeft -> {
                logger.v { "New antler points left ${intent.newAntlerPointsLeft}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerPointsLeft = intent.newAntlerPointsLeft
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerPointsRight -> {
                logger.v { "New antler points right ${intent.newAntlerPointsRight}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerPointsRight = intent.newAntlerPointsRight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersLost -> {
                logger.v { "New antlers lost ${intent.newAntlersLost}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersLost = intent.newAntlersLost
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersGirth -> {
                logger.v { "New antlers girth ${intent.newAntlersGirth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersGirth = intent.newAntlersGirth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerShaftWidth -> {
                logger.v { "New antler shaft width ${intent.newAntlerShaftWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlerShaftWidth = intent.newAntlerShaftWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersLength -> {
                logger.v { "New antlers length ${intent.newAntlersLength}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersLength = intent.newAntlersLength
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersInnerWidth -> {
                logger.v { "New antlers inner width ${intent.newAntlersInnerWidth}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                        antlersInnerWidth = intent.newAntlersInnerWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAlone -> {
                logger.v { "New alone ${intent.newAlone}" }
                val specimen = getFirstSpecimenOrCreate(harvest).copy(alone = intent.newAlone)
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeActor -> {
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
            is ModifyHarvestIntent.ChangeActorHunterNumber -> {
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
            is ModifyHarvestIntent.ChangeIsOwnHarvest,
            is ModifyHarvestIntent.ChangeSelectedClub,
            is ModifyHarvestIntent.ChangeSelectedClubOfficialCode,
            is ModifyHarvestIntent.LaunchPermitSelection,
            is ModifyHarvestIntent.ClearSelectedPermit,
            is ModifyHarvestIntent.SelectPermit,
            is ModifyHarvestIntent.ChangeSpecimenAmount,
            is ModifyHarvestIntent.ChangeSpecies,
            is ModifyHarvestIntent.SetEntityImage,
            is ModifyHarvestIntent.ChangeSpecimenData,
            is ModifyHarvestIntent.ChangeWeight,
            is ModifyHarvestIntent.ChangeWildBoarFeedingPlace,
            is ModifyHarvestIntent.ChangeIsTaigaBean,
            is ModifyHarvestIntent.ChangeGreySealHuntingMethod,
            is ModifyHarvestIntent.ChangeDescription -> {
                logger.w { "Unexpected intent $intent" }
                harvest
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
        harvest: CommonHarvestData,
        huntingDayId: GroupHuntingDayId,
        huntingDays: List<GroupHuntingDay>,
    ): CommonHarvestData {
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

    private fun getFirstSpecimenOrCreate(harvest: CommonHarvestData) : CommonSpecimenData {
        return harvest.specimens.firstOrNull()
                ?: CommonSpecimenData().ensureDefaultValuesAreSet()
    }

    private fun updateFirstSpecimen(harvest: CommonHarvestData, specimen: CommonSpecimenData): CommonHarvestData {
        return harvest.copy(
            specimens = listOf(specimen) + harvest.specimens.drop(1)
        )
    }

    internal fun createViewModel(
        harvest: CommonHarvestData,
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

        val validationErrors = harvestValidator.validate(
                harvest, huntingDays, huntingGroupPermit, fieldsToBeDisplayed)

        val harvestIsValid = validationErrors.isEmpty()

        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors)

        return ModifyGroupHarvestViewModel(
                harvest = harvest,
                huntingGroupStatus = huntingGroupStatus,
                huntingGroupMembers = huntingGroupMembers,
                huntingGroupPermit = huntingGroupPermit,
                huntingDays = huntingDays,
                fields = fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
                    fieldProducer.createField(fieldSpecification, harvest, huntingGroupMembers)
                },
                harvestIsValid = harvestIsValid,
                huntingGroupArea = huntingGroupArea,
        )
    }

    private fun List<FieldSpecification<CommonHarvestField>>.injectErrorLabels(
        validationErrors: List<CommonHarvestValidator.Error>
    ): List<FieldSpecification<CommonHarvestField>> {
        if (validationErrors.isEmpty()) {
            return this
        }

        val result = mutableListOf<FieldSpecification<CommonHarvestField>>()

        forEach { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                CommonHarvestField.HUNTING_DAY_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(CommonHarvestValidator.Error.TIME_NOT_WITHIN_HUNTING_DAY)) {
                        result.add(CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY.noRequirement())
                    }
                }
                CommonHarvestField.DATE_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(CommonHarvestValidator.Error.DATE_NOT_WITHIN_PERMIT)) {
                        result.add(CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT.noRequirement())
                    } else if (validationErrors.contains(CommonHarvestValidator.Error.DATETIME_IN_FUTURE)) {
                        result.add(CommonHarvestField.ERROR_DATETIME_IN_FUTURE.noRequirement())
                    }
                }
                CommonHarvestField.SPECIES_CODE,
                CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT,
                CommonHarvestField.ERROR_DATETIME_IN_FUTURE,
                CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                CommonHarvestField.LOCATION,
                CommonHarvestField.DEER_HUNTING_TYPE,
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
                CommonHarvestField.OWN_HARVEST,
                CommonHarvestField.ACTOR_HUNTER_NUMBER,
                CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                CommonHarvestField.SELECTED_CLUB,
                CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE,
                CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE_INFO_OR_ERROR,
                CommonHarvestField.GENDER,
                CommonHarvestField.AGE,
                CommonHarvestField.NOT_EDIBLE,
                CommonHarvestField.ADDITIONAL_INFORMATION,
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
                CommonHarvestField.HEADLINE_SHOOTER,
                CommonHarvestField.HEADLINE_SPECIMEN,
                CommonHarvestField.WEIGHT,
                CommonHarvestField.WEIGHT_ESTIMATED,
                CommonHarvestField.WEIGHT_MEASURED,
                CommonHarvestField.FITNESS_CLASS,
                CommonHarvestField.ANTLER_INSTRUCTIONS,
                CommonHarvestField.ANTLERS_TYPE,
                CommonHarvestField.ANTLERS_WIDTH,
                CommonHarvestField.ANTLER_POINTS_LEFT,
                CommonHarvestField.ANTLER_POINTS_RIGHT,
                CommonHarvestField.ANTLERS_LOST,
                CommonHarvestField.ANTLERS_GIRTH,
                CommonHarvestField.ANTLER_SHAFT_WIDTH,
                CommonHarvestField.ANTLERS_LENGTH,
                CommonHarvestField.ANTLERS_INNER_WIDTH,
                CommonHarvestField.ACTOR,
                CommonHarvestField.AUTHOR,
                CommonHarvestField.ALONE,
                CommonHarvestField.SELECT_PERMIT,
                CommonHarvestField.PERMIT_INFORMATION,
                CommonHarvestField.PERMIT_REQUIRED_NOTIFICATION,
                CommonHarvestField.SPECIES_CODE_AND_IMAGE,
                CommonHarvestField.HARVEST_REPORT_STATE,
                CommonHarvestField.SPECIMEN_AMOUNT,
                CommonHarvestField.SPECIMENS,
                CommonHarvestField.WILD_BOAR_FEEDING_PLACE,
                CommonHarvestField.GREY_SEAL_HUNTING_METHOD,
                CommonHarvestField.IS_TAIGA_BEAN_GOOSE,
                CommonHarvestField.DESCRIPTION -> {
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

    internal fun ModifyGroupHarvestViewModel.getValidatedHarvestDataOrNull(): CommonHarvestData? {
        val displayedFields = GroupHuntingHarvestFields.getFieldsToBeDisplayed(
            GroupHuntingHarvestFields.Context(
                harvest = harvest,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT
            )
        )

        val validationErrors = harvestValidator.validate(
            harvest = harvest,
            huntingDays = huntingDays,
            huntingGroupPermit = huntingGroupPermit,
            displayedFields = displayedFields
        )
        if (validationErrors.isNotEmpty()) {
            return null
        }

        return harvest.createCopyWithFields(displayedFields)

    }

    internal fun CommonHarvestData.ensureDefaultValuesAreSet(): CommonHarvestData {
        return copy(
            specimens = specimens.map { it.ensureDefaultValuesAreSet() }
        )
    }

    internal fun CommonSpecimenData.ensureDefaultValuesAreSet(): CommonSpecimenData {
        return copy(
            notEdible = notEdible ?: false,
            alone = alone ?: false,
            antlersLost = antlersLost ?: false
        )
    }

    internal fun CommonHarvestData.selectInitialHuntingDayForHarvest(
        huntingDays: List<GroupHuntingDay>
    ): CommonHarvestData {
        // automatic hunting day selection is relevant only for moose and only
        // if there's no hunting day.
        // - deer animals will get their hunting day when saving harvest
        // - don't replace previously set hunting day in any case
        if (!species.isMoose() || huntingDayId != null) {
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
        return getLoadedViewModelOrNull()?.harvest?.species?.isMoose() == true
    }

    private fun CommonHarvestData.createCopyWithFields(
        fields: List<FieldSpecification<CommonHarvestField>>,
    ): CommonHarvestData {
        return copy(
                // copy deer hunting type even though its requirement status most likely wont change
                // - species code cannot be changed and thus deer hunting type visibility should
                //   remain unchanged
                deerHuntingType = deerHuntingType
                    .takeIf { fields.contains(CommonHarvestField.DEER_HUNTING_TYPE) }
                        ?: BackendEnum.create(null),
                deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription
                    .takeIf { fields.contains(CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION) },
                specimens = specimens.createCopyWithFields(fields),
        )
    }

    private fun List<CommonSpecimenData>.createCopyWithFields(
        fields: List<FieldSpecification<CommonHarvestField>>,
    ): List<CommonSpecimenData> {
        // fields are determined based on the first specimen i.e. only create copy of the first specimen.
        // Let rest of the specimen be what they currently are (there shouldn't be any though)
        val specimen = first()

        val firstSpecimen = CommonSpecimenData(
            remoteId = specimen.remoteId,
            revision = specimen.revision,
            gender = specimen.gender.takeIf { fields.contains(CommonHarvestField.GENDER) },
            age = specimen.age.takeIf { fields.contains(CommonHarvestField.AGE) },
            stateOfHealth = null,
            marking = null,
            lengthOfPaw = null,
            widthOfPaw = null,
            weight = specimen.weight.takeIf { fields.contains(CommonHarvestField.WEIGHT) },
            weightEstimated = specimen.weightEstimated.takeIf { fields.contains(CommonHarvestField.WEIGHT_ESTIMATED) },
            weightMeasured = specimen.weightMeasured.takeIf { fields.contains(CommonHarvestField.WEIGHT_MEASURED) },
            fitnessClass = specimen.fitnessClass.takeIf { fields.contains(CommonHarvestField.FITNESS_CLASS) },
            antlersLost = specimen.antlersLost.takeIf { fields.contains(CommonHarvestField.ANTLERS_LOST) },
            antlersType = specimen.antlersType.takeIf { fields.contains(CommonHarvestField.ANTLERS_TYPE) },
            antlersWidth = specimen.antlersWidth.takeIf { fields.contains(CommonHarvestField.ANTLERS_WIDTH) },
            antlerPointsLeft = specimen.antlerPointsLeft.takeIf { fields.contains(CommonHarvestField.ANTLER_POINTS_LEFT) },
            antlerPointsRight = specimen.antlerPointsRight.takeIf { fields.contains(CommonHarvestField.ANTLER_POINTS_RIGHT) },
            antlersGirth = specimen.antlersGirth.takeIf { fields.contains(CommonHarvestField.ANTLERS_GIRTH) },
            antlersLength = specimen.antlersLength.takeIf { fields.contains(CommonHarvestField.ANTLERS_LENGTH) },
            antlersInnerWidth = specimen.antlersInnerWidth.takeIf { fields.contains(CommonHarvestField.ANTLERS_INNER_WIDTH) },
            antlerShaftWidth = specimen.antlerShaftWidth.takeIf { fields.contains(CommonHarvestField.ANTLER_SHAFT_WIDTH) },
            notEdible = specimen.notEdible.takeIf { fields.contains(CommonHarvestField.NOT_EDIBLE) },
            alone = specimen.alone.takeIf { fields.contains(CommonHarvestField.ALONE) },
            additionalInfo = specimen.additionalInfo.takeIf { fields.contains(CommonHarvestField.ADDITIONAL_INFORMATION) }
        )

        return listOf(firstSpecimen) + drop(1)
    }

    @Serializable
    data class SavedState internal constructor(
        internal val harvestData: CommonHarvestData,
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

