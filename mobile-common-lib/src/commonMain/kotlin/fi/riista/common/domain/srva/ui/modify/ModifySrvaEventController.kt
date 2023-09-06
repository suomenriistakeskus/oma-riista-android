@file:Suppress("SpellCheckingInspection")

package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.keepNonEmpty
import fi.riista.common.domain.srva.SaveSrvaResponse
import fi.riista.common.domain.srva.SrvaContext
import fi.riista.common.domain.srva.SrvaEventOperationResponse
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.toCommonSrvaMethod
import fi.riista.common.domain.srva.model.toSrvaEvent
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.domain.srva.ui.SrvaEventFields
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.replace
import kotlinx.serialization.Serializable

/**
 * A controller for modifying [CommonSrvaEventData] information
 */
abstract class ModifySrvaEventController internal constructor(
    private val metadataProvider: MetadataProvider,
    protected val srvaContext: SrvaContext,
    protected val localDateTimeProvider: LocalDateTimeProvider,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ModifySrvaEventViewModel>(),
    IntentHandler<ModifySrvaEventIntent>,
    HasUnreproducibleState<ModifySrvaEventController.SavedState> {

    private val srvaEventFields = SrvaEventFields(metadataProvider = metadataProvider)
    val eventDispatchers: ModifySrvaEventDispatcher by lazy {
        ModifySrvaEventToIntentMapper(intentHandler = this)
    }

    internal var restoredSrvaEventData: CommonSrvaEventData? = null

    /**
     * Can the SRVA event location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the SRVA event.
     */
    protected var srvaEventLocationCanBeUpdatedAutomatically: Boolean = true

    private val pendingIntents = mutableListOf<ModifySrvaEventIntent>()

    private val fieldProducer = EditSrvaEventFieldProducer(
        metadataProvider = metadataProvider,
        stringProvider = stringProvider,
        localDateTimeProvider = localDateTimeProvider,
    )


    /**
     * Saves the srva event to local database and optionally tries to send it to backend.
     */
    suspend fun saveSrvaEvent(updateToBackend: Boolean): SaveSrvaResponse {
        val srvaEventToBeSaved = getSrvaEventToSaveOrNull()?.copy(modified = true) ?: kotlin.run {
            return SaveSrvaResponse(
                databaseSaveResponse = SrvaEventOperationResponse.Error("no valid SRVA event")
            )
        }

        return when (val saveResponse = srvaContext.saveSrvaEvent(srvaEventToBeSaved)) {
            is SrvaEventOperationResponse.Error,
            is SrvaEventOperationResponse.SaveFailure,
            is SrvaEventOperationResponse.NetworkFailure -> {
                SaveSrvaResponse(databaseSaveResponse = saveResponse)
            }
            is SrvaEventOperationResponse.Success -> {
                if (updateToBackend) {
                    val networkResponse = srvaContext.sendSrvaEventToBackend(srvaEvent = saveResponse.srvaEvent)
                    SaveSrvaResponse(
                        databaseSaveResponse = saveResponse,
                        networkSaveResponse = networkResponse
                    )
                } else {
                    SaveSrvaResponse(databaseSaveResponse = saveResponse)
                }
            }
        }
    }

    override fun handleIntent(intent: ModifySrvaEventIntent) {
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
        intent: ModifySrvaEventIntent,
        viewModel: ModifySrvaEventViewModel,
    ): ModifySrvaEventViewModel {
        val srvaEvent = viewModel.srvaEvent

        val updatedEvent = when (intent) {
            is ModifySrvaEventIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    srvaEventLocationCanBeUpdatedAutomatically = false
                }

                srvaEvent.copy(location = intent.location.asKnownLocation())
            }
            is ModifySrvaEventIntent.ChangeSpecies -> {
                if (fieldProducer.selectableSrvaSpecies.contains(candidate = intent.species)) {
                    srvaEvent.copy(
                        species = intent.species
                    ).withValidatedEventTypeDetail()
                } else {
                    srvaEvent
                }
            }
            is ModifySrvaEventIntent.SetEntityImage -> {
                srvaEvent.copy(
                    images = srvaEvent.images.withNewPrimaryImage(intent.image)
                )
            }
            is ModifySrvaEventIntent.ChangeOtherSpeciesDescription -> {
                srvaEvent.copy(otherSpeciesDescription = intent.description)
            }
            is ModifySrvaEventIntent.ChangeDateAndTime -> {
                srvaEvent.copy(pointOfTime = intent.dateAndTime)
            }
            is ModifySrvaEventIntent.ChangeSpecimenAmount -> {
                srvaEvent.copy(
                    specimenAmount = intent.specimenAmount
                )
            }
            is ModifySrvaEventIntent.ChangeSpecimenData ->
                srvaEvent.copy(
                    specimenAmount = intent.specimenData.specimens.size,
                    specimens = intent.specimenData.specimens.keepNonEmpty()
                )
            is ModifySrvaEventIntent.ChangeEventCategory -> {
                // changing category requires clearing also the event type, event result and methods as those
                // depend on newly selected category
                val possibleMethods = metadataProvider.srvaMetadata.getCategory(intent.eventCategory)
                    ?.possibleMethods
                    ?.map { it.toCommonSrvaMethod(selected = false) }
                    ?: listOf()

                srvaEvent.copy(
                    eventCategory = intent.eventCategory,
                    eventType = BackendEnum.create(null),
                    eventTypeDetail = BackendEnum.create(null),
                    eventResult = BackendEnum.create(null),
                    eventResultDetail = BackendEnum.create(null),
                    methods = possibleMethods,
                )
            }
            is ModifySrvaEventIntent.ChangeDeportationOrderNumber ->
                srvaEvent.copy(deportationOrderNumber = intent.deportationOrderNumber)
            is ModifySrvaEventIntent.ChangeEventType ->
                srvaEvent.copy(eventType = intent.eventType)
                    .withValidatedEventTypeDetail()
            is ModifySrvaEventIntent.ChangeOtherEventTypeDescription ->
                srvaEvent.copy(otherEventTypeDescription = intent.description)
            is ModifySrvaEventIntent.ChangeEventTypeDetail ->
                srvaEvent.copy(eventTypeDetail = intent.eventTypeDetail)
            is ModifySrvaEventIntent.ChangeOtherEventTypeDetailDescription ->
                srvaEvent.copy(otherEventTypeDetailDescription = intent.description)
            is ModifySrvaEventIntent.ChangeEventResult ->
                srvaEvent.copy(eventResult = intent.eventResult)
                    .withValidatedEventResultDetail()
            is ModifySrvaEventIntent.ChangeEventResultDetail ->
                srvaEvent.copy(eventResultDetail = intent.eventResultDetail)
            is ModifySrvaEventIntent.ChangeMethodSelectionStatus -> {
                val method = srvaEvent.methods.getOrNull(intent.methodIndex)

                if (method != null) {
                    srvaEvent.copy(
                        methods = srvaEvent.methods.replace(
                            index = intent.methodIndex,
                            item = method.copy(selected = intent.selected)
                        )
                    )
                } else {
                    logger.w { "Invalid SRVA method index ${intent.methodIndex}" }
                    srvaEvent
                }
            }
            is ModifySrvaEventIntent.ChangeOtherMethodDescription ->
                srvaEvent.copy(otherMethodDescription = intent.description)
            is ModifySrvaEventIntent.ChangePersonCount ->
                srvaEvent.copy(personCount = intent.personCount)
            is ModifySrvaEventIntent.ChangeHoursSpent ->
                srvaEvent.copy(hoursSpent = intent.hoursSpent)
            is ModifySrvaEventIntent.ChangeDescription ->
                srvaEvent.copy(description = intent.description)
        }

        return createViewModel(
            srvaEvent = updatedEvent,
        )
    }

    private fun CommonSrvaEventData.withValidatedEventTypeDetail(): CommonSrvaEventData {
        val srvaCategory = metadataProvider.srvaMetadata.getCategory(type = eventCategory)
        val allowedEventTypeDetails = srvaCategory?.possibleEventTypeDetailsFor(srvaEvent = this) ?: listOf()
        return if (allowedEventTypeDetails.contains(eventTypeDetail)) {
            this
        } else {
            copy(
                eventTypeDetail = BackendEnum.create(null),
            )
        }
    }

    private fun CommonSrvaEventData.withValidatedEventResultDetail(): CommonSrvaEventData {
        val srvaCategory = metadataProvider.srvaMetadata.getCategory(type = eventCategory)
        val allowedEventResultDetails = srvaCategory?.possibleEventResultDetailsFor(srvaEvent = this) ?: listOf()
        return if (allowedEventResultDetails.contains(eventResultDetail)) {
            this
        } else {
            copy(
                eventResultDetail = BackendEnum.create(null),
            )
        }
    }

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.srvaEvent?.let {
            SavedState(
                srvaEvent = it,
                srvaEventLocationCanBeUpdatedAutomatically = srvaEventLocationCanBeUpdatedAutomatically
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredSrvaEventData = state.srvaEvent
        srvaEventLocationCanBeUpdatedAutomatically = state.srvaEventLocationCanBeUpdatedAutomatically
    }

    internal fun createViewModel(
        srvaEvent: CommonSrvaEventData,
    ): ModifySrvaEventViewModel {

        val fieldsToBeDisplayed = srvaEventFields.getFieldsToBeDisplayed(
            SrvaEventFields.Context(
                srvaEvent = srvaEvent,
                mode = SrvaEventFields.Context.Mode.EDIT,
            )
        )

        val validationErrors = CommonSrvaEventValidator.validate(
            srvaEvent = srvaEvent,
            srvaMetadata = metadataProvider.srvaMetadata,
            displayedFields = fieldsToBeDisplayed,
        )

        val srvaEventIsValid = validationErrors.isEmpty()

        return ModifySrvaEventViewModel(
            srvaEvent = srvaEvent,
            fields = fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
                fieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    srvaEvent = srvaEvent
                )
            },
            srvaEventIsValid = srvaEventIsValid
        )
    }

    protected fun ModifySrvaEventViewModel.applyPendingIntents(): ModifySrvaEventViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    /**
     * Attempts to get the srva event and prepare it to be saved.
     *
     * Validates the current srva event data and ensures it only contains data that is allowed
     * to be saved.
     */
    private fun getSrvaEventToSaveOrNull(): CommonSrvaEvent? {
        val viewModel = getLoadedViewModelOrNull() ?: kotlin.run { return null }

        return prepareForSave(
            saveCandidate = viewModel.srvaEvent,
        )?.toSrvaEvent()
    }

    /**
     * Prepares the given ([saveCandidate]) srva event to be saved. Returns either valid srva event with data
     * that can be saved or `null` if srva event could not be validated.
     */
    private fun prepareForSave(
        saveCandidate: CommonSrvaEventData,
    ): CommonSrvaEventData? {
        val displayedFields = srvaEventFields.getFieldsToBeDisplayed(
            SrvaEventFields.Context(
                srvaEvent = saveCandidate,
                mode = SrvaEventFields.Context.Mode.EDIT
            )
        )

        if (!saveCandidate.isValidAgainstFields(displayedFields)) {
            logger.w { "Srva event save-candidate was not valid." }
            return null
        }

        val srvaEventToSave = saveCandidate.prepareForSaveWithFields(
            fields = displayedFields.map { it.fieldId.type }.toSet()
        ) ?: kotlin.run {
            logger.w { "Failed to obtain srva-event-to-save." }
            return null
        }

        if (!srvaEventToSave.isValidAgainstFields(displayedFields)) {
            logger.w { "Srva event save-candidate was not valid." }
            return null
        }

        return srvaEventToSave
    }

    private fun CommonSrvaEventData.isValidAgainstFields(
        displayedFields: List<FieldSpecification<SrvaEventField>>,
    ): Boolean {
        val validationErrors = CommonSrvaEventValidator.validate(
            srvaEvent = this,
            srvaMetadata = metadataProvider.srvaMetadata,
            displayedFields = displayedFields,
        )
        return validationErrors.isEmpty()
    }

    private fun CommonSrvaEventData.prepareForSaveWithFields(
        fields: Set<SrvaEventField.Type>,
    ): CommonSrvaEventData? {
        return CommonSrvaEventData(
            localId = localId,
            localUrl = localUrl,
            remoteId = remoteId,
            revision = revision,
            mobileClientRefId = mobileClientRefId,
            srvaSpecVersion = srvaSpecVersion,
            state = state,
            rhyId = rhyId,
            canEdit = canEdit,
            location = location.takeIf {
                fields.contains(SrvaEventField.Type.LOCATION)
            } ?: CommonLocation.Unknown,
            pointOfTime = pointOfTime.also {
                if (!fields.contains(SrvaEventField.Type.DATE_AND_TIME)) {
                    logger.e { "Fields didn't contain DATE_AND_TIME!" }
                    return null
                }
            },
            author = author,
            approver = approver.takeIf {
                fields.contains(SrvaEventField.Type.APPROVER_OR_REJECTOR)
            },
            species = species.takeIf {
                fields.contains(SrvaEventField.Type.SPECIES_CODE)
            } ?: Species.Unknown,
            otherSpeciesDescription = otherSpeciesDescription.takeIf {
                fields.contains(SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION)
            },
            specimenAmount = specimenAmount.takeIf {
                fields.contains(SrvaEventField.Type.SPECIMEN_AMOUNT)
            },
            specimens = specimens.let {
                if (fields.contains(SrvaEventField.Type.SPECIMEN)) {
                    it.keepNonEmpty()
                } else {
                    emptyList()
                }
            },
            eventCategory = eventCategory.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_CATEGORY)
            } ?: BackendEnum.create(null),
            deportationOrderNumber = deportationOrderNumber.takeIf {
                fields.contains(SrvaEventField.Type.DEPORTATION_ORDER_NUMBER)
            },
            eventType = eventType.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_TYPE)
            } ?: BackendEnum.create(null),
            otherEventTypeDescription = otherEventTypeDescription.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION)
            },
            eventTypeDetail = eventTypeDetail.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_TYPE_DETAIL)
            } ?: BackendEnum.create(null),
            otherEventTypeDetailDescription = otherEventTypeDetailDescription.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION)
            },
            eventResult = eventResult.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_RESULT)
            } ?: BackendEnum.create(null),
            eventResultDetail = eventResultDetail.takeIf {
                fields.contains(SrvaEventField.Type.EVENT_RESULT_DETAIL)
            } ?: BackendEnum.create(null),
            methods = methods, // always include
            otherMethodDescription = otherMethodDescription.takeIf {
                fields.contains(SrvaEventField.Type.OTHER_METHOD_DESCRIPTION)
            },
            personCount = personCount.takeIf {
                fields.contains(SrvaEventField.Type.PERSON_COUNT)
            },
            hoursSpent = hoursSpent.takeIf {
                fields.contains(SrvaEventField.Type.HOURS_SPENT)
            },
            description = description.takeIf {
                fields.contains(SrvaEventField.Type.DESCRIPTION)
            },
            images = images,
            modified = modified,
            deleted = deleted,
        )
    }

    @Serializable
    data class SavedState internal constructor(
        internal val srvaEvent: CommonSrvaEventData,
        internal val srvaEventLocationCanBeUpdatedAutomatically: Boolean,
    )

    companion object {
        private val logger by getLogger(ModifySrvaEventController::class)
    }
}

