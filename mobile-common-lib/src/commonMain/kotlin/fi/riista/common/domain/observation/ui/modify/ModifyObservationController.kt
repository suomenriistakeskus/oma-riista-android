package fi.riista.common.domain.observation.ui.modify

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.toCommonObservation
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.ObservationFields
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import fi.riista.common.util.firstAndOnly
import fi.riista.common.util.hasSameElements
import fi.riista.common.util.withNumberOfElements
import kotlinx.serialization.Serializable

/**
 * A controller for modifying [CommonObservationData] information
 */
abstract class ModifyObservationController internal constructor(
    private val userContext: UserContext,
    private val metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
    localDateTimeProvider: LocalDateTimeProvider
) : ControllerWithLoadableModel<ModifyObservationViewModel>(),
    IntentHandler<ModifyObservationIntent>,
    HasUnreproducibleState<ModifyObservationController.SavedState> {

    constructor(
        userContext: UserContext,
        metadataProvider: MetadataProvider,
        stringProvider: StringProvider,
    ): this(
        userContext = userContext,
        metadataProvider = metadataProvider,
        stringProvider = stringProvider,
        localDateTimeProvider = SystemDateTimeProvider()
    )


    private val observationFields = ObservationFields(metadataProvider = metadataProvider)
    val eventDispatchers: ModifyObservationEventDispatcher by lazy {
        ModifyObservationEventToIntentMapper(intentHandler = this)
    }

    internal var restoredObservationData: CommonObservationData? = null

    /**
     * Can the observation location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the observation event.
     */
    protected var observationLocationCanBeUpdatedAutomatically: Boolean = true

    private val pendingIntents = mutableListOf<ModifyObservationIntent>()

    private val fieldProducer = ModifyObservationFieldProducer(
        carnivoreAuthorityInformationProvider = userContext,
        metadataProvider = metadataProvider,
        stringProvider = stringProvider,
        localDateTimeProvider = localDateTimeProvider,
    )

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    fun getValidatedObservation(): CommonObservation? {
        return getLoadedViewModelOrNull()
            ?.getValidatedObservationOrNull()
            ?.toCommonObservation()
    }

    override fun handleIntent(intent: ModifyObservationIntent) {
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
        intent: ModifyObservationIntent,
        viewModel: ModifyObservationViewModel,
    ): ModifyObservationViewModel {
        val observation = viewModel.observation

        val updatedObservation = when (intent) {
            is ModifyObservationIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    observationLocationCanBeUpdatedAutomatically = false
                }

                observation.copy(
                    location = intent.location.asKnownLocation(),
                    // invalidate distance to residence as we cannot have that information in mobile client
                    inYardDistanceToResidence = null,
                )
            }
            is ModifyObservationIntent.ChangeSpecies -> {
                if (fieldProducer.selectableObservationSpecies.contains(candidate = intent.species)) {
                    observation.changeSpecies(newSpecies = intent.species)
                        // possible specimens fields depend on selected species
                        .withInvalidatedSpecimens()
                } else {
                    observation
                }
            }
            is ModifyObservationIntent.SetEntityImage -> {
                observation.copy(
                    images = observation.images.withNewPrimaryImage(intent.image)
                )
            }
            is ModifyObservationIntent.ChangeDateAndTime -> {
                observation.copy(pointOfTime = intent.dateAndTime)
            }
            is ModifyObservationIntent.ChangeObservationCategory -> {
                observation
                    .changeObservationCategory(newObservationCategory = intent.observationCategory)
                    .withInvalidatedSpecimens() // possible specimens fields depend on selected observation category
            }
            is ModifyObservationIntent.ChangeDeerHuntingType ->
                observation.copy(deerHuntingType = intent.deerHuntingType)
            is ModifyObservationIntent.ChangeDeerHuntingOtherTypeDescription ->
                observation.copy(deerHuntingOtherTypeDescription = intent.deerHuntingOtherTypeDescription)
            is ModifyObservationIntent.ChangeObservationType ->
                observation
                    .copy(observationType = intent.observationType)
                    .withInvalidatedSpecimens() // possible specimens fields depend on selected observation type
            is ModifyObservationIntent.ChangeSpecimenAmount -> {
                if (intent.specimenAmount == null) {
                    observation.copy(
                        totalSpecimenAmount = intent.specimenAmount
                    )
                } else {
                    val updatedSpecimens = observation.specimensOrEmptyList.withNumberOfElements(intent.specimenAmount) {
                        CommonSpecimenData()
                    }
                    observation.updateSpecimens(
                        specimens = updatedSpecimens
                    )
                }
            }
            is ModifyObservationIntent.ChangeSpecimenData ->
                observation.updateSpecimens(
                    specimens = intent.specimenData.specimens,
                )

            is ModifyObservationIntent.ChangeMooselikeMaleAmount ->
                observation.copy(mooselikeMaleAmount = intent.mooselikeMaleAmount)
            is ModifyObservationIntent.ChangeMooselikeFemaleAmount ->
                observation.copy(mooselikeFemaleAmount = intent.mooselikeFemaleAmount)
            is ModifyObservationIntent.ChangeMooselikeFemale1CalfAmount ->
                observation.copy(mooselikeFemale1CalfAmount = intent.mooselikeFemale1CalfAmount)
            is ModifyObservationIntent.ChangeMooselikeFemale2CalfsAmount ->
                observation.copy(mooselikeFemale2CalfsAmount = intent.mooselikeFemale2CalfsAmount)
            is ModifyObservationIntent.ChangeMooselikeFemale3CalfsAmount ->
                observation.copy(mooselikeFemale3CalfsAmount = intent.mooselikeFemale3CalfsAmount)
            is ModifyObservationIntent.ChangeMooselikeFemale4CalfsAmount ->
                observation.copy(mooselikeFemale4CalfsAmount = intent.mooselikeFemale4CalfsAmount)
            is ModifyObservationIntent.ChangeMooselikeCalfAmount ->
                observation.copy(mooselikeCalfAmount = intent.mooselikeCalfAmount)
            is ModifyObservationIntent.ChangeMooselikeUnknownSpecimenAmount ->
                observation.copy(mooselikeUnknownSpecimenAmount = intent.mooselikeUnknownSpecimenAmount)

            is ModifyObservationIntent.ChangeVerifiedByCarnivoreAuthority ->
                observation.copy(verifiedByCarnivoreAuthority = intent.verifiedByCarnivoreAuthority)
            is ModifyObservationIntent.ChangeObserverName ->
                observation.copy(observerName = intent.observerName)
            is ModifyObservationIntent.ChangeObserverPhoneNumber ->
                observation.copy(observerPhoneNumber = intent.observerPhoneNumber)
            is ModifyObservationIntent.ChangeOfficialAdditionaInformation ->
                observation.copy(officialAdditionalInfo = intent.officialAdditionalInformation)
            is ModifyObservationIntent.ChangeDescription ->
                observation.copy(description = intent.description)
        }

        return createViewModel(
            observation = updatedObservation,
        )
    }

    /**
     * Changes the species to the specified one and updates other fields if necessary.
     */
    internal fun CommonObservationData.changeSpecies(newSpecies: Species): CommonObservationData {
        val metadataForNewSpecies = metadataProvider.observationMetadata.getSpeciesMetadata(species = newSpecies)
            ?: kotlin.run {
                logger.w { "Failed to obtain observation metadata for the species $newSpecies" }
                return copy(
                    species = newSpecies,
                    observationCategory = ObservationCategory.NORMAL.toBackendEnum(),
                )
            }

        val oldObservationCategories = metadataProvider.observationMetadata.getSpeciesMetadata(species = this.species)
            ?.getAvailableObservationCategories()
            ?: listOf()

        val observationCategories = metadataForNewSpecies.getAvailableObservationCategories()
        val sameObservationCategories = observationCategories.hasSameElements(oldObservationCategories)

        val newObservationCategory: BackendEnum<ObservationCategory> = when {
            observationCategories.contains(observationCategory) && sameObservationCategories -> observationCategory
            else -> observationCategories.firstAndOnly() ?: BackendEnum.create(null)
        }

        val observationTypes = metadataForNewSpecies.getObservationTypes(observationCategory = newObservationCategory)
        val newObservationType: BackendEnum<ObservationType> = when {
            observationTypes.contains(observationType) -> observationType
            else -> observationTypes.firstAndOnly() ?: BackendEnum.create(null)
        }

        return copy(
            species = newSpecies,
            observationCategory = newObservationCategory,
            observationType = newObservationType,
        )
    }

    /**
     * Changes the observation category to the specified one and updates other fields if necessary.
     */
    private fun CommonObservationData.changeObservationCategory(
        newObservationCategory: BackendEnum<ObservationCategory>
    ): CommonObservationData {
        val speciesMetadata = metadataProvider.observationMetadata.getSpeciesMetadata(species = species)
            ?: kotlin.run {
                logger.w { "Failed to obtain observation metadata for the current species $species" }
                return copy(
                    observationCategory = newObservationCategory,
                )
            }

        val observationTypes = speciesMetadata.getObservationTypes(observationCategory = newObservationCategory)
        val newObservationType: BackendEnum<ObservationType> = when {
            observationTypes.contains(observationType) -> observationType
            else -> observationTypes.firstAndOnly() ?: BackendEnum.create(null)
        }

        return copy(
            observationCategory = newObservationCategory,
            observationType = newObservationType,
        )
    }

    private fun CommonObservationData.withInvalidatedSpecimens(): CommonObservationData {
        return updateSpecimens(
            specimens = List(size = totalSpecimenAmount ?: 0) {
                CommonSpecimenData()
            },
        )
    }

    private fun CommonObservationData.updateSpecimens(
        specimens: List<CommonSpecimenData>,
    ): CommonObservationData {
        return copy(
            totalSpecimenAmount = specimens.size,
            specimens = specimens,
            // invalidate pack / litter as currently we don't calculate those in the mobile
            // client (we could, but not implemented right now)
            pack = null,
            litter = null,
        )
    }

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.observation?.let {
            SavedState(
                observation = it,
                observationLocationCanBeUpdatedAutomatically = observationLocationCanBeUpdatedAutomatically
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredObservationData = state.observation
        observationLocationCanBeUpdatedAutomatically = state.observationLocationCanBeUpdatedAutomatically
    }

    internal fun createViewModel(
        observation: CommonObservationData,
    ): ModifyObservationViewModel {

        val fieldsToBeDisplayed = observationFields.getFieldsToBeDisplayed(
            ObservationFields.Context(
                observation = observation,
                userIsCarnivoreAuthority = userContext.userIsCarnivoreAuthority,
                mode = ObservationFields.Context.Mode.EDIT,
            )
        )

        val validationErrors = CommonObservationValidator.validate(
            observation = observation,
            observationMetadata = metadataProvider.observationMetadata,
            displayedFields = fieldsToBeDisplayed,
        )

        val observationIsValid = validationErrors.isEmpty()

        return ModifyObservationViewModel(
            observation = observation,
            fields = fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
                fieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    observation = observation
                )
            },
            observationIsValid = observationIsValid
        )
    }

    protected fun ModifyObservationViewModel.applyPendingIntents(): ModifyObservationViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    private fun ModifyObservationViewModel.getValidatedObservationOrNull(): CommonObservationData? {
        val displayedFields = observationFields.getFieldsToBeDisplayed(
            ObservationFields.Context(
                observation = observation,
                userIsCarnivoreAuthority = userContext.userIsCarnivoreAuthority,
                mode = ObservationFields.Context.Mode.EDIT
            )
        )

        val validationErrors = CommonObservationValidator.validate(
            observation = observation,
            observationMetadata = metadataProvider.observationMetadata,
            displayedFields = displayedFields,
        )
        if (validationErrors.isNotEmpty()) {
            return null
        }

        return observation.createCopyWithFields(displayedFields)

    }

    private fun CommonObservationData.createCopyWithFields(
        fields: List<FieldSpecification<CommonObservationField>>,
    ): CommonObservationData? {
        val fieldTypes: Set<CommonObservationField> = fields.map { it.fieldId }.toSet()

        // check absolutely necessary fields
        val alwaysPresentFields = CommonObservationField.fieldsWithPresence(CommonObservationField.Presence.ALWAYS)
        if (!fieldTypes.containsAll(alwaysPresentFields)) {
            logger.w { "Fields didn't contain all always-present fields!" }
            return null
        }

        val observationCopy = copy(
            location = location.takeIf {
                fieldTypes.contains(CommonObservationField.LOCATION)
            } ?: CommonLocation.Unknown,
            species = species.takeIf {
                fieldTypes.contains(CommonObservationField.SPECIES_AND_IMAGE)
            } ?: Species.Unknown,
            pointOfTime = pointOfTime.also {
                if (!fieldTypes.contains(CommonObservationField.DATE_AND_TIME)) {
                    logger.e { "Fields didn't contain DATE_AND_TIME!" }
                    return null
                }
            },
            observationType = observationType.takeIf {
                fieldTypes.contains(CommonObservationField.OBSERVATION_TYPE)
            } ?: BackendEnum.create(null),

            // SPECIAL CASE:
            // always add observation category as that information needs to be present even though field is not displayed
            observationCategory = observationCategory,

            deerHuntingType = deerHuntingType.takeIf {
                fieldTypes.contains(CommonObservationField.DEER_HUNTING_TYPE)
            } ?: BackendEnum.create(null),
            deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription.takeIf {
                fieldTypes.contains(CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION)
            },
            totalSpecimenAmount = totalSpecimenAmount.takeIf {
                fieldTypes.contains(CommonObservationField.SPECIMEN_AMOUNT)
            },
            specimens = specimens.takeIf {
                fieldTypes.contains(CommonObservationField.SPECIMENS)
            },
            mooselikeMaleAmount = (mooselikeMaleAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_MALE_AMOUNT)
            },
            mooselikeFemaleAmount = (mooselikeFemaleAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT)
            },
            mooselikeFemale1CalfAmount = (mooselikeFemale1CalfAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT)
            },
            mooselikeFemale2CalfsAmount = (mooselikeFemale2CalfsAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT)
            },
            mooselikeFemale3CalfsAmount = (mooselikeFemale3CalfsAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT)
            },
            mooselikeFemale4CalfsAmount = (mooselikeFemale4CalfsAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT)
            },
            mooselikeCalfAmount = (mooselikeCalfAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_CALF_AMOUNT)
            },
            mooselikeUnknownSpecimenAmount = (mooselikeUnknownSpecimenAmount ?: 0).takeIf {
                fieldTypes.contains(CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT)
            },
            verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY)
            },
            observerName = observerName.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_OBSERVER_NAME)
            },
            observerPhoneNumber = observerPhoneNumber.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_OBSERVER_PHONENUMBER)
            },
            officialAdditionalInfo = officialAdditionalInfo.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO)
            },
            inYardDistanceToResidence = inYardDistanceToResidence.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE)
            },
            litter = litter.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_LITTER)
            },
            pack = pack.takeIf {
                fieldTypes.contains(CommonObservationField.TASSU_PACK)
            },
            description = description.takeIf {
                fieldTypes.contains(CommonObservationField.DESCRIPTION)
            },
        )

        return observationCopy
    }

    @Serializable
    data class SavedState internal constructor(
        internal val observation: CommonObservationData,
        internal val observationLocationCanBeUpdatedAutomatically: Boolean,
    )

    companion object {
        private val logger by getLogger(ModifyObservationController::class)
    }
}

