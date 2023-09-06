package fi.riista.common.domain.harvest.ui.modify

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.asGuest
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.SaveHarvestResponse
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.model.toCommonHarvest
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.common.HarvestSpecimenFieldProducer
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields
import fi.riista.common.domain.harvest.validation.CommonHarvestValidator
import fi.riista.common.domain.huntingclub.selectableForEntries.HuntingClubsSelectableForEntries
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.asSearchableOrganization
import fi.riista.common.domain.model.keepNonEmpty
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.domain.season.HarvestSeasons
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.preferences.Preferences
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.serialization.Serializable

/**
 * A controller for modifying [CommonHarvestData] information
 */
abstract class ModifyHarvestController internal constructor(
    harvestSeasons: HarvestSeasons,
    protected val harvestContext: HarvestContext,
    protected val currentTimeProvider: LocalDateTimeProvider,
    internal val harvestPermitProvider: HarvestPermitProvider,
    private val selectableHuntingClubs: HuntingClubsSelectableForEntries,
    languageProvider: LanguageProvider,
    preferences: Preferences,
    speciesResolver: SpeciesResolver,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ModifyHarvestViewModel>(),
    IntentHandler<ModifyHarvestIntent>,
    HasUnreproducibleState<ModifyHarvestController.SavedState> {

    private val harvestFields = CommonHarvestFields(
        harvestSeasons, speciesResolver, preferences
    )

    private val harvestValidator = CommonHarvestValidator(currentTimeProvider, speciesResolver)

    val eventDispatchers: ModifyHarvestEventDispatcher by lazy {
        ModifyHarvestEventToIntentMapper(intentHandler = this)
    }

    internal var restoredHarvestData: CommonHarvestData? = null

    private val _modifyHarvestActionHandler = AtomicReference<ModifyHarvestActionHandler?>(null)
    var modifyHarvestActionHandler: ModifyHarvestActionHandler?
        get() = _modifyHarvestActionHandler.get()
        set(value) {
            _modifyHarvestActionHandler.set(value)
        }

    /**
     * Can the harvest location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the harvest event.
     */
    protected var harvestLocationCanBeUpdatedAutomatically: Boolean = true

    private val pendingIntents = mutableListOf<ModifyHarvestIntent>()

    private val fieldProducer = ModifyHarvestFieldProducer(
        canChangeSpecies = true,
        harvestPermitProvider = harvestPermitProvider,
        huntingClubsSelectableForHarvests = selectableHuntingClubs,
        stringProvider = stringProvider,
        languageProvider = languageProvider,
        currentDateTimeProvider = currentTimeProvider,
    )

    /**
     * Saves the harvest to local database and optionally tries to send it to backend.
     */
    suspend fun saveHarvest(updateToBackend: Boolean): SaveHarvestResponse {
        val harvestToBeSaved = getHarvestToSaveOrNull()?.copy(modified = true) ?: kotlin.run {
            return SaveHarvestResponse(
                databaseSaveResponse = HarvestOperationResponse.Error("Not valid harvest"),
            )
        }

        return when (val saveResponse = harvestContext.saveHarvest(harvestToBeSaved)) {
            is HarvestOperationResponse.Error,
            is HarvestOperationResponse.SaveFailure,
            is HarvestOperationResponse.NetworkFailure -> {
                SaveHarvestResponse(databaseSaveResponse = saveResponse)
            }
            is HarvestOperationResponse.Success -> {
                if (updateToBackend) {
                    val networkResponse = harvestContext.sendHarvestToBackend(harvest = saveResponse.harvest)
                    SaveHarvestResponse(
                        databaseSaveResponse = saveResponse,
                        networkSaveResponse = networkResponse,
                    )
                } else {
                    SaveHarvestResponse(databaseSaveResponse = saveResponse)
                }
            }
        }
    }

    fun updateViewModel() {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(
                ViewModelLoadStatus.Loaded(
                    viewModel = createViewModel(
                        harvest = viewModel.harvest,
                        permit = viewModel.permit,
                        ownHarvest = viewModel.ownHarvest,
                        shooters = viewModel.shooters,
                    )
                )
            )
        }
    }

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
        viewModel: ModifyHarvestViewModel,
    ): ModifyHarvestViewModel {
        val harvest = viewModel.harvest

        // the current permit. Should be updated if permit is changed.
        var currentPermit = viewModel.permit
        var ownHarvest = viewModel.ownHarvest

        val updatedHarvest = when (intent) {
            is ModifyHarvestIntent.LaunchPermitSelection -> {
                requestModifyHarvestAction(
                    action = ModifyHarvestAction.SelectPermit(
                        currentPermitNumber = harvest.permitNumber.takeIf { intent.restrictToCurrentPermitNumber }
                    )
                )
                return viewModel
            }
            is ModifyHarvestIntent.ClearSelectedPermit -> {
                // clear the permit so that it won't be used in validation
                currentPermit = null

                harvest.copy(
                    permitNumber = null,
                    permitType = null,
                )
            }
            is ModifyHarvestIntent.SelectPermit -> {
                val newSpecies: Species = intent.speciesCode
                    ?.let { speciesCode ->
                        Species.Known(speciesCode)
                            .takeIf { intent.permit.isAvailableForSpecies(it) }
                    }
                    ?: harvest.species.takeIf { intent.permit.isAvailableForSpecies(it) }
                    ?: Species.Unknown

                // determine whether species changed as that information is needed when deciding
                // whether specimens need to be cleared or not
                val speciesChanged = newSpecies.knownSpeciesCodeOrNull() != harvest.species.knownSpeciesCodeOrNull()
                currentPermit = intent.permit

                val harvestWithPermit = harvest.copy(
                    species = newSpecies,
                    permitNumber = intent.permit.permitNumber,
                    permitType = intent.permit.permitType
                )

                if (speciesChanged) {
                    // invalidate specimens if species was changed
                    harvestWithPermit.withInvalidatedSpecimens()
                } else {
                    harvestWithPermit
                }
            }
            is ModifyHarvestIntent.ChangeSpecies -> {
                if (fieldProducer.selectableHarvestSpecies.contains(candidate = intent.species)) {
                    val harvestWithChangedSpecies = if (currentPermit?.isAvailableForSpecies(intent.species) == true) {
                        harvest.copy(
                            species = intent.species,
                            // don't clear permit information
                        )
                    } else {
                        // clear the permit so that it won't be used in validation
                        currentPermit = null

                        harvest.copy(
                            species = intent.species,
                            permitNumber = null,
                            permitType = null,
                            harvestReportRequired = false,
                            harvestReportState = BackendEnum.create(null),
                            stateAcceptedToHarvestPermit = BackendEnum.create(null),
                        )
                    }

                    // possible specimens fields depend on selected species
                    harvestWithChangedSpecies.withInvalidatedSpecimens()
                } else {
                    harvest
                }
            }
            is ModifyHarvestIntent.SetEntityImage -> {
                harvest.copy(
                    images = harvest.images.withNewPrimaryImage(intent.image)
                )
            }
            is ModifyHarvestIntent.ChangeSpecimenAmount -> {
                // explicitly don't alter specimens, only amount
                // - harvest data allowed to contain different amount of specimens than amount
                //   (allows user to change amount: 10->1->""->2->20 (first 10 being the original ones)
                harvest.copy(
                    amount = intent.specimenAmount
                )
            }
            is ModifyHarvestIntent.ChangeSpecimenData ->
                harvest.updateSpecimens(
                    specimens = intent.specimenData.specimens,
                )
            is ModifyHarvestIntent.ChangeDescription ->
                harvest.copy(description = intent.description)
            is ModifyHarvestIntent.ChangeAdditionalInformation -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    additionalInfo = intent.newAdditionalInformation
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeGender -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    gender = BackendEnum.create(intent.newGender)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeDateAndTime -> {
                harvest.copy(pointOfTime = intent.newDateAndTime.coerceAtMost(currentTimeProvider.now()))
            }
            is ModifyHarvestIntent.ChangeDeerHuntingType -> {
                harvest.copy(deerHuntingType = intent.deerHuntingType)
            }
            is ModifyHarvestIntent.ChangeDeerHuntingOtherTypeDescription -> {
                harvest.copy(deerHuntingOtherTypeDescription = intent.deerHuntingOtherTypeDescription)
            }
            is ModifyHarvestIntent.ChangeAge -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    age = BackendEnum.create(intent.newAge)
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    harvestLocationCanBeUpdatedAutomatically = false
                }

                harvest.copy(location = intent.newLocation.asKnownLocation())
            }
            is ModifyHarvestIntent.ChangeNotEdible -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    notEdible = intent.newNotEdible
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWeight -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    weight = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWeightEstimated -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    weightEstimated = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWeightMeasured -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    weightMeasured = intent.newWeight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeFitnessClass -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    fitnessClass = intent.newFitnessClass
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersType -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersType = intent.newAntlersType
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersWidth -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersWidth = intent.newAntlersWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerPointsLeft -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlerPointsLeft = intent.newAntlerPointsLeft
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerPointsRight -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlerPointsRight = intent.newAntlerPointsRight
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersLost -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersLost = intent.newAntlersLost
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersGirth -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersGirth = intent.newAntlersGirth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlerShaftWidth -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlerShaftWidth = intent.newAntlerShaftWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersLength -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersLength = intent.newAntlersLength
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAntlersInnerWidth -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(
                    antlersInnerWidth = intent.newAntlersInnerWidth
                )
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeAlone -> {
                val specimen = getFirstSpecimenOrCreate(harvest).copy(alone = intent.newAlone)
                updateFirstSpecimen(harvest, specimen)
            }
            is ModifyHarvestIntent.ChangeWildBoarFeedingPlace ->
                harvest.copy(feedingPlace = intent.feedingPlace)
            is ModifyHarvestIntent.ChangeIsTaigaBean ->
                harvest.copy(taigaBeanGoose = intent.isTaigaBeanGoose)
            is ModifyHarvestIntent.ChangeGreySealHuntingMethod ->
                harvest.copy(greySealHuntingMethod = intent.greySealHuntingMethod)
            is ModifyHarvestIntent.ChangeIsOwnHarvest -> {
                ownHarvest = intent.isOwnHarvest
                harvest
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
            is ModifyHarvestIntent.ChangeActor -> {
                if (intent.newActor.id == GroupHuntingPerson.SearchingByHunterNumber.ID) {
                    harvest.copy(actorInfo = GroupHuntingPerson.SearchingByHunterNumber(
                        hunterNumber = "",
                        status = GroupHuntingPerson.SearchingByHunterNumber.Status.ENTERING_HUNTER_NUMBER
                    ))
                } else {
                    val shooter = viewModel.shooters.firstOrNull { it.id == intent.newActor.id }
                    if (shooter != null) {
                        harvest.copy(actorInfo = GroupHuntingPerson.Guest(shooter))
                    } else {
                        harvest
                    }
                }
            }
            is ModifyHarvestIntent.ChangeSelectedClub -> {
                when (intent.newSelectedClub.id) {
                    SearchableOrganization.Unknown.ID -> {
                        harvest.copy(selectedClub = SearchableOrganization.Unknown)
                    }
                    SearchableOrganization.Searching.ID -> {
                        // continue from previous search (if exists)
                        val previousSearch = harvest.selectedClub as? SearchableOrganization.Searching
                        harvest.copy(
                            selectedClub = previousSearch ?: SearchableOrganization.Searching.startSearch()
                        )
                    }
                    else -> {
                        val huntingClubId = intent.newSelectedClub.id
                        val huntingClub = selectableHuntingClubs.findSelectableClub(huntingClubId)
                        if (huntingClub != null) {
                            harvest.copy(selectedClub = SearchableOrganization.Found(huntingClub))
                        } else {
                            logger.w { "No club found with id $huntingClubId" }
                            harvest
                        }
                    }
                }
            }
            is ModifyHarvestIntent.ChangeSelectedClubOfficialCode -> {
                (viewModel.harvest.selectedClub as? SearchableOrganization.Searching)
                    ?.let { currentSearch ->
                        val updatedSearch = currentSearch.withUpdatedOfficialCode(intent.officialCode.toString())
                        if (updatedSearch.status == SearchableOrganization.Searching.Status.VALID_OFFICIAL_CODE_ENTERED) {
                            // we've got a valid official code -> initiate search
                            updateViewModelSuspended {
                                searchHuntingClubByOfficialCode()
                            }
                        }
                        harvest.copy(selectedClub = updatedSearch)
                    }
                    ?: harvest
            }
            is ModifyHarvestIntent.ChangeTime,
            is ModifyHarvestIntent.ChangeHuntingDay -> harvest
        }

        return createViewModel(
            harvest = updatedHarvest,
            permit = currentPermit,
            ownHarvest = ownHarvest,
            shooters = viewModel.shooters,
        )
    }

    private fun requestModifyHarvestAction(action: ModifyHarvestAction) {
        val handler = modifyHarvestActionHandler ?: kotlin.run {
            logger.w { "No action handler for modify harvest actions!" }
            return
        }

        handler.handleModifyHarvestAction(action)
    }

    private fun CommonHarvestData.withInvalidatedSpecimens(): CommonHarvestData {
        return updateSpecimens(
            specimens = List(size = amount ?: 1) {
                CommonSpecimenData().ensureDefaultValuesAreSet()
            },
        )
    }

    private fun CommonHarvestData.updateSpecimens(
        specimens: List<CommonSpecimenData>,
    ): CommonHarvestData {
        return copy(
            amount = specimens.size,
            specimens = specimens.keepNonEmpty(),
        )
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

    internal fun CommonSpecimenData.ensureDefaultValuesAreSet(): CommonSpecimenData {
        return copy(
            notEdible = notEdible ?: false,
            alone = alone ?: false,
            antlersLost = antlersLost ?: false
        )
    }

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.harvest?.let {
            SavedState(
                harvest = it,
                harvestLocationCanBeUpdatedAutomatically = harvestLocationCanBeUpdatedAutomatically,
                nonPersistentSelectableClubs = selectableHuntingClubs.getNonPersistentClubs()
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredHarvestData = state.harvest
        harvestLocationCanBeUpdatedAutomatically = state.harvestLocationCanBeUpdatedAutomatically
        selectableHuntingClubs.restoreNonPersistentClubs(state.nonPersistentSelectableClubs)
    }

    internal fun createViewModel(
        harvest: CommonHarvestData,
        permit: CommonHarvestPermit?,
        shooters: List<PersonWithHunterNumber>,
        ownHarvest: Boolean,
    ): ModifyHarvestViewModel {
        val harvestContext = harvestFields.createContext(
            harvest = harvest,
            mode = CommonHarvestFields.Context.Mode.EDIT,
            ownHarvest = ownHarvest,
        )
        var fieldsToBeDisplayed = harvestFields.getFieldsToBeDisplayed(harvestContext)

        val validationErrors = harvestValidator.validate(
            harvest = harvest,
            permit = permit,
            harvestReportingType = harvestContext.harvestReportingType,
            displayedFields = fieldsToBeDisplayed,
        )

        val harvestIsValid = validationErrors.isEmpty()

        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors)

        return ModifyHarvestViewModel(
            harvest = harvest,
            permit = permit,
            shooters = shooters,
            ownHarvest = ownHarvest,
            fields = fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
                fieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    harvest = harvest,
                    harvestReportingType = harvestContext.harvestReportingType,
                    shooters = shooters,
                    ownHarvest = harvestContext.ownHarvest,
                )
            },
            harvestIsValid = harvestIsValid
        )
    }

    protected fun ModifyHarvestViewModel.applyPendingIntents(): ModifyHarvestViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    /**
     * Attempts to get the harvest and prepare it to be saved.
     *
     * Validates the current harvest data and ensures it only contains data that is allowed
     * to be saved.
     */
    private fun getHarvestToSaveOrNull(): CommonHarvest? {
        val viewModel = getLoadedViewModelOrNull() ?: kotlin.run { return null }

        return prepareForSave(
            saveCandidate = viewModel.harvest,
            permit = viewModel.permit,
            ownHarvest = viewModel.ownHarvest,
        )?.toCommonHarvest()
    }

    /**
     * Prepares the given ([saveCandidate]) harvest to be saved. Returns either valid harvest with data
     * that can be saved or `null` if harvest could not be validated.
     */
    private fun prepareForSave(
        saveCandidate: CommonHarvestData,
        permit: CommonHarvestPermit?,
        ownHarvest: Boolean,
    ): CommonHarvestData? {
        val harvestContext = harvestFields.createContext(
            harvest = saveCandidate,
            mode = CommonHarvestFields.Context.Mode.EDIT,
            ownHarvest = ownHarvest,
        )
        val displayedFields = harvestFields.getFieldsToBeDisplayed(harvestContext)

        if (!saveCandidate.isValidAgainstFields(displayedFields, permit, harvestContext.harvestReportingType)) {
            logger.w { "Harvest save-candidate was not valid." }
            return null
        }

        val harvestToSave = saveCandidate.prepareForSaveWithFields(
            fields = displayedFields.map { it.fieldId }.toSet(),
            ownHarvest = ownHarvest,
        ) ?: kotlin.run {
            logger.w { "Failed to obtain harvest-to-save." }
            return null
        }

        if (!harvestToSave.isValidAgainstFields(displayedFields, permit, harvestContext.harvestReportingType)) {
            logger.w { "Harvest-to-save was not valid." }
            return null
        }

        return harvestToSave

    }

    private fun CommonHarvestData.isValidAgainstFields(
        displayedFields: List<FieldSpecification<CommonHarvestField>>,
        permit: CommonHarvestPermit?,
        harvestReportingType: HarvestReportingType,
    ): Boolean {
        val validationErrors = harvestValidator.validate(
            harvest = this,
            permit = permit,
            harvestReportingType = harvestReportingType,
            displayedFields = displayedFields,
        )

        return validationErrors.isEmpty()
    }

    private fun CommonHarvestData.prepareForSaveWithFields(
        fields: Set<CommonHarvestField>,
        ownHarvest: Boolean,
    ): CommonHarvestData? {
        // sanitize amount i.e. use the value provided by the user if available and amount field was displayed.
        // If the field was not displayed (e.g. most mammals), fallback to single
        val sanitizedSpecimenAmount = amount.takeIf {
            // this may produce invalid harvest data if amount field is not displayed. It
            // is necessary to take this into account when converting CommonHarvestData to
            // CommonHarvest (i.e. default to amount of specimens if amount is missing)
            fields.contains(CommonHarvestField.SPECIMEN_AMOUNT)
        }
            // fallback to defaulting one specimen
            ?: 1


        val harvestCopy = CommonHarvestData(
            localId = localId,
            localUrl = localUrl,
            id = id,
            rev = rev,
            species = species.takeIf {
                fields.contains(CommonHarvestField.SPECIES_CODE_AND_IMAGE) ||
                        fields.contains(CommonHarvestField.SPECIES_CODE)
            } ?: Species.Unknown,
            location = location.takeIf {
                fields.contains(CommonHarvestField.LOCATION)
            } ?: CommonLocation.Unknown,
            pointOfTime = pointOfTime.also {
                if (!fields.contains(CommonHarvestField.DATE_AND_TIME)) {
                    logger.e { "Fields didn't contain DATE_AND_TIME!" }
                    return null
                }
            },
            description = description.takeIf {
                fields.contains(CommonHarvestField.DESCRIPTION)
            },
            canEdit = canEdit,
            modified = modified,
            deleted = deleted,
            images = images,
            specimens = specimens.prepareForSaveWithFields(
                speciesCode = species.knownSpeciesCodeOrNull(),
                specimenAmount = sanitizedSpecimenAmount,
                fields = fields
            ),
            amount = sanitizedSpecimenAmount,
            huntingDayId = huntingDayId,
            authorInfo = authorInfo,
            // GroupHuntingPerson.Guest is used when the actor is not the same as the author. Use Unknown when it is own
            actorInfo = if (ownHarvest) { GroupHuntingPerson.Unknown } else { actorInfo },
            selectedClub = selectedClub.takeIf { fields.contains(CommonHarvestField.SELECTED_CLUB) }
                ?: SearchableOrganization.Unknown,
            harvestSpecVersion = harvestSpecVersion,
            harvestReportRequired = harvestReportRequired,
            harvestReportState = harvestReportState,
            permitNumber = permitNumber.takeIf {
                fields.contains(CommonHarvestField.PERMIT_INFORMATION)
            },
            permitType = permitType.takeIf {
                fields.contains(CommonHarvestField.PERMIT_INFORMATION)
            },
            stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit,
            deerHuntingType = deerHuntingType.takeIf {
                fields.contains(CommonHarvestField.DEER_HUNTING_TYPE)
            } ?: BackendEnum.create(null),
            deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription.takeIf {
                fields.contains(CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION)
            },
            mobileClientRefId = mobileClientRefId,
            harvestReportDone = harvestReportDone,
            rejected = rejected,
            feedingPlace = feedingPlace.takeIf {
                fields.contains(CommonHarvestField.WILD_BOAR_FEEDING_PLACE)
            },
            taigaBeanGoose = taigaBeanGoose.takeIf {
                fields.contains(CommonHarvestField.IS_TAIGA_BEAN_GOOSE)
            },
            greySealHuntingMethod = greySealHuntingMethod.takeIf {
                fields.contains(CommonHarvestField.GREY_SEAL_HUNTING_METHOD)
            } ?: BackendEnum.create(null),
        )

        return harvestCopy
    }

    private fun List<CommonSpecimenData>.prepareForSaveWithFields(
        speciesCode: SpeciesCode?,
        specimenAmount: Int,
        fields: Set<CommonHarvestField>,
    ): List<CommonSpecimenData> {
        if (specimenAmount == 0) {
            // should not happen as there should always be at least one specimen (according to current knowledge)
            return emptyList()
        }

        // strategy:
        // 1. iterate the specimens and only keep data that should be kept
        // 2. drop empty specimens
        // 3. ensure total specimen amount is not exceeded
        return this.map { it.createCopyWithFields(speciesCode, fields) }
            .keepNonEmpty()
            .take(specimenAmount)
    }

    private fun CommonSpecimenData.createCopyWithFields(
        speciesCode: SpeciesCode?,
        fields: Set<CommonHarvestField>,
    ): CommonSpecimenData {
        // two possibilities here. Either specimen is modified directly in harvest view or it is modified in
        // external view (EditSpecimensController). In the latter case the specimen fields are NOT determined
        // by the fieldTypes but HarvestSpecimenFieldProducer.
        //
        // Check the latter case first
        if (fields.contains(CommonHarvestField.SPECIMENS)) {
            val specimenFieldTypes = HarvestSpecimenFieldProducer.getSpecimenFieldTypes(speciesCode = speciesCode)

            // don't copy in order to make sure extra data is not included
            return CommonSpecimenData(
                remoteId = remoteId,
                revision = revision,
                gender = gender.takeIf { specimenFieldTypes.contains(SpecimenFieldType.GENDER) },
                age = age.takeIf { specimenFieldTypes.contains(SpecimenFieldType.AGE) },
                stateOfHealth = stateOfHealth.takeIf { specimenFieldTypes.contains(SpecimenFieldType.STATE_OF_HEALTH) },
                marking = marking.takeIf { specimenFieldTypes.contains(SpecimenFieldType.MARKING) },
                lengthOfPaw = lengthOfPaw.takeIf { specimenFieldTypes.contains(SpecimenFieldType.LENGTH_OF_PAW) },
                widthOfPaw = widthOfPaw.takeIf { specimenFieldTypes.contains(SpecimenFieldType.WIDTH_OF_PAW) },
                weight = weight.takeIf { specimenFieldTypes.contains(SpecimenFieldType.WEIGHT) },
                weightEstimated = null,
                weightMeasured = null,
                fitnessClass = null,
                antlersLost = null,
                antlersType = null,
                antlersWidth = null,
                antlerPointsLeft = null,
                antlerPointsRight = null,
                antlersGirth = null,
                antlersLength = null,
                antlersInnerWidth = null,
                antlerShaftWidth = null,
                notEdible = null,
                alone = null,
                additionalInfo = null,
            )
        }

        // don't copy in order to make sure extra data is not included
        return CommonSpecimenData(
            remoteId = remoteId,
            revision = revision,
            gender = gender.takeIf { fields.contains(CommonHarvestField.GENDER) },
            age = age.takeIf { fields.contains(CommonHarvestField.AGE) },
            stateOfHealth = null, // observation related field
            marking = null, // observation related field
            lengthOfPaw = null, // observation related field
            widthOfPaw = null, // observation related field
            weight = weight.takeIf { fields.contains(CommonHarvestField.WEIGHT) },
            weightEstimated = weightEstimated.takeIf { fields.contains(CommonHarvestField.WEIGHT_ESTIMATED) },
            weightMeasured = weightMeasured.takeIf { fields.contains(CommonHarvestField.WEIGHT_MEASURED) },
            fitnessClass = fitnessClass.takeIf { fields.contains(CommonHarvestField.FITNESS_CLASS) },
            antlersLost = antlersLost.takeIf { fields.contains(CommonHarvestField.ANTLERS_LOST) },
            antlersType = antlersType.takeIf { fields.contains(CommonHarvestField.ANTLERS_TYPE) },
            antlersWidth = antlersWidth.takeIf { fields.contains(CommonHarvestField.ANTLERS_WIDTH) },
            antlerPointsLeft = antlerPointsLeft.takeIf { fields.contains(CommonHarvestField.ANTLER_POINTS_LEFT) },
            antlerPointsRight = antlerPointsRight.takeIf { fields.contains(CommonHarvestField.ANTLER_POINTS_RIGHT) },
            antlersGirth = antlersGirth.takeIf { fields.contains(CommonHarvestField.ANTLERS_GIRTH) },
            antlersLength = antlersLength.takeIf { fields.contains(CommonHarvestField.ANTLERS_LENGTH) },
            antlersInnerWidth = antlersInnerWidth.takeIf { fields.contains(CommonHarvestField.ANTLERS_INNER_WIDTH) },
            antlerShaftWidth = antlerShaftWidth.takeIf { fields.contains(CommonHarvestField.ANTLER_SHAFT_WIDTH) },
            notEdible = notEdible.takeIf { fields.contains(CommonHarvestField.NOT_EDIBLE) },
            alone = alone.takeIf { fields.contains(CommonHarvestField.ALONE) },
            additionalInfo = additionalInfo.takeIf { fields.contains(CommonHarvestField.ADDITIONAL_INFORMATION) },
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
                CommonHarvestField.DATE_AND_TIME -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(CommonHarvestValidator.Error.DATE_NOT_WITHIN_PERMIT)) {
                        result.add(CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT.noRequirement())
                    }
                }
                CommonHarvestField.HUNTING_DAY_AND_TIME,
                CommonHarvestField.SPECIES_CODE,
                CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT,
                CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                CommonHarvestField.ERROR_DATETIME_IN_FUTURE,
                CommonHarvestField.LOCATION,
                CommonHarvestField.DEER_HUNTING_TYPE,
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
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
                CommonHarvestField.OWN_HARVEST,
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
                permit = viewModel.permit,
                ownHarvest = viewModel.ownHarvest,
                shooters = viewModel.shooters,
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

        val actorInfo = hunter?.asGuest()
            ?: searchingHunterNumber.copy(status = GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED)

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = createViewModel(
                harvest = viewModel.harvest.copy(actorInfo = actorInfo),
                permit = viewModel.permit,
                ownHarvest = viewModel.ownHarvest,
                shooters = viewModel.shooters,
            )
        ))
    }

    private suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumber? {
        return harvestContext.searchPersonByHunterNumber(hunterNumber)?.toPersonWithHunterNumber()
    }

    private suspend fun searchHuntingClubByOfficialCode() {
        var viewModel = getLoadedViewModelOrNull()
            ?: kotlin.run {
                logger.d { "Cannot search club by official code. No loaded viewmodel!" }
                return
            }

        val initialOfficialCodeSearch = viewModel.harvest.selectedClub as? SearchableOrganization.Searching
        val officialCode = initialOfficialCodeSearch?.officialCode
            ?: kotlin.run {
                logger.d { "No hunter number, cannot search actor." }
                return
            }

        val searchingState = initialOfficialCodeSearch.copy(
            status = SearchableOrganization.Searching.Status.SEARCHING
        )

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = createViewModel(
                harvest = viewModel.harvest.copy(selectedClub = searchingState),
                permit = viewModel.permit,
                ownHarvest = viewModel.ownHarvest,
                shooters = viewModel.shooters,
            )
        ))

        val huntingClub = selectableHuntingClubs.searchClubByOfficialCode(officialCode)

        viewModel = getLoadedViewModelOrNull()
            ?: kotlin.run {
                logger.d { "Cannot search club by official code. No loaded viewmodel!" }
                return
            }

        // only update viewModel if user has not updated viewModel while we we're fetching
        // hunting club
        if (searchingState != viewModel.harvest.selectedClub) {
            logger.d { "User updated selected club while fetching hunter, discarding result" }
            return
        }

        val searchResult = huntingClub?.asSearchableOrganization()
            ?: searchingState.copy(status = SearchableOrganization.Searching.Status.SEARCH_FAILED)

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = createViewModel(
                harvest = viewModel.harvest.copy(selectedClub = searchResult),
                permit = viewModel.permit,
                ownHarvest = viewModel.ownHarvest,
                shooters = viewModel.shooters,
            )
        ))
    }

    @Serializable
    data class SavedState internal constructor(
        internal val harvest: CommonHarvestData,
        internal val harvestLocationCanBeUpdatedAutomatically: Boolean,
        internal val nonPersistentSelectableClubs: List<Organization>
    )

    companion object {
        private val logger by getLogger(ModifyHarvestController::class)
    }
}

