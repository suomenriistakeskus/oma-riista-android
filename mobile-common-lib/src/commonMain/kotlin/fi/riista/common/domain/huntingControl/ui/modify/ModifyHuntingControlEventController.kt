package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlEventOperationResponse
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlEventType
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.model.IdentifiesRhy
import fi.riista.common.domain.huntingControl.model.toHuntingControlEventInspector
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.FileSaveResult
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.isWithinPeriod
import fi.riista.common.model.toPeriodDate
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.contains
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ModifyHuntingControlEventController(
    protected val stringProvider: StringProvider,
    protected val huntingControlContext: HuntingControlContext,
    protected val userContext: UserContext,
    private val commonFileProvider: CommonFileProvider,
    private val identifiesRhy: IdentifiesRhy,
) : ControllerWithLoadableModel<ModifyHuntingControlEventViewModel>(),
    IntentHandler<ModifyHuntingControlEventIntent>,
    HasUnreproducibleState<ModifyHuntingControlEventController.SavedState> {

    val eventDispatchers: ModifyHuntingControlEventEventDispatcher by lazy {
        ModifyHuntingControlEventEventToIntentMapper(intentHandler = this)
    }

    private val fieldProducer = ModifyHuntingControlEventFieldProducer(stringProvider = stringProvider)

    protected var restoredEvent: HuntingControlEventData? = null

    /**
     * Can the event location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the event.
     */
    var eventLocationCanBeMovedAutomatically = true

    private val pendingIntents = mutableListOf<ModifyHuntingControlEventIntent>()

    suspend fun saveHuntingControlEvent(updateToBackend: Boolean): HuntingControlEventOperationResponse {
        val eventData = getLoadedViewModelOrNull()?.event ?: kotlin.run {
            logger.w { "Failed to obtain hunting control event from viewModel in order to create it" }
            return HuntingControlEventOperationResponse.Error
        }

        val rhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = identifiesRhy,
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the RHY (id: ${identifiesRhy.rhyId})" }
            return HuntingControlEventOperationResponse.Error
        }

        moveAttachmentsToAttachmentsDirectory()

        val eventToBeSaved = eventData.copy(modified = true)
        val response = rhyContext.saveHuntingControlEvent(eventToBeSaved)
        if (response is HuntingControlEventOperationResponse.Success && updateToBackend) {
            huntingControlContext.sendHuntingControlEventToBackend(response.event)
        }
        return response
    }

    fun getAttachment(field: HuntingControlEventField): HuntingControlAttachment? {
        val viewModel = getLoadedViewModelOrNull() ?: return null

        if (viewModel.event.attachments.size > field.index) {
            return viewModel.event.attachments[field.index]
        }
        return null
    }

    private suspend fun moveAttachmentsToAttachmentsDirectory() {
        getLoadedViewModelOrNull()?.event?.attachments?.forEach {
            it.moveToAttachmentsDirectory()
        }
    }

    private suspend fun HuntingControlAttachment.moveToAttachmentsDirectory(): FileSaveResult {
        val fileUuid = uuid ?: kotlin.run {
            return FileSaveResult.SaveFailed(exception = null)
        }

        return suspendCoroutine { continuation ->
            commonFileProvider.moveTemporaryFileTo(
                targetDirectory = CommonFileProvider.Directory.ATTACHMENTS,
                fileUuid = fileUuid
            ) { fileSaveResult ->
                continuation.resume(fileSaveResult)
            }
        }
    }

    override fun handleIntent(intent: ModifyHuntingControlEventIntent) {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(
                ViewModelLoadStatus.Loaded(
                    viewModel = handleIntent(intent, viewModel)
                )
            )
        } else {
            pendingIntents.add(intent)
        }
    }

    private fun handleIntent(
        intent: ModifyHuntingControlEventIntent,
        viewModel: ModifyHuntingControlEventViewModel,
    ): ModifyHuntingControlEventViewModel {

        val event = viewModel.event
        var selfInspectorWarning = viewModel.selfInspectorWarning

        val updatedEvent = when (intent) {
            is ModifyHuntingControlEventIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    eventLocationCanBeMovedAutomatically = false
                }
                event.copy(location = intent.newLocation.asKnownLocation())
            }
            is ModifyHuntingControlEventIntent.ChangeEventType -> {
                event.copy(eventType = intent.newEvenType.toBackendEnum())
            }
            is ModifyHuntingControlEventIntent.ChangeLocationDescription -> {
                event.copy(locationDescription = intent.newDescription)
            }
            is ModifyHuntingControlEventIntent.ChangeNumberOfCustomers -> {
                event.copy(customerCount = intent.numberOfCustomers)
            }
            is ModifyHuntingControlEventIntent.ChangeDate -> {
                // Remove inspectors that are not available on the new date
                val availableGameWardens = getGameWardendsForDate(
                    date = event.date,
                    allWardens = viewModel.gameWardens
                )
                event.copy(date = intent.newDate, inspectors = event.inspectors.filter { inspector ->
                    availableGameWardens.contains { inspector.id == it.remoteId }
                })
            }
            is ModifyHuntingControlEventIntent.ChangeStartTime -> {
                val endTime = event.endTime
                val startTime = endTime?.let { intent.newStartTime.coerceAtMost(it) } ?: intent.newStartTime
                event.copy(startTime = startTime)
            }
            is ModifyHuntingControlEventIntent.ChangeEndTime -> {
                val startTime = event.startTime
                val endTime = startTime?.let { intent.newEndTime.coerceAtLeast(it) } ?: intent.newEndTime
                event.copy(endTime = endTime)
            }
            is ModifyHuntingControlEventIntent.ChangeWolfTerritory -> {
                event.copy(wolfTerritory = intent.newWolfTerritory)
            }
            is ModifyHuntingControlEventIntent.ChangeOtherPartisipants -> {
                event.copy(otherParticipants = intent.newOtherPartisipants)
            }
            is ModifyHuntingControlEventIntent.ChangeInspectors -> {
                val inspectors = intent.newInspectors.mapNotNull { stringWithId ->
                    viewModel.gameWardens.firstOrNull { it.remoteId == stringWithId.id }?.toHuntingControlEventInspector()
                }
                val userId = userContext.userInformation?.id
                if (userId != null && inspectors.none { it.id == userId }) {
                    selfInspectorWarning = true
                    event
                } else {
                    selfInspectorWarning = false
                    event.copy(inspectors = inspectors)
                }
            }
            is ModifyHuntingControlEventIntent.RemoveInspectors -> {
                val inspectors = event.inspectors.filter { eventInspector ->
                    intent.removeInspector.id != eventInspector.id
                }
                val userId = userContext.userInformation?.id
                if (userId != null && inspectors.none { it.id == userId }) {
                    selfInspectorWarning = true
                    event
                } else {
                    selfInspectorWarning = false
                    event.copy(inspectors = inspectors)
                }
            }
            is ModifyHuntingControlEventIntent.ToggleCooperationType -> {
                val toggled = intent.toggledCooperationType.toBackendEnum<HuntingControlCooperationType>()
                if (event.cooperationTypes.contains(toggled)) {
                    event.copy(cooperationTypes = event.cooperationTypes.filter {
                        it != toggled
                    })
                } else {
                    event.copy(cooperationTypes = event.cooperationTypes + listOf(toggled))
                }
            }
            is ModifyHuntingControlEventIntent.ChangeNumberOfProofOrders -> {
                event.copy(proofOrderCount = intent.numberOfProofOrders)
            }
            is ModifyHuntingControlEventIntent.ChangeDescription -> {
                event.copy(description = intent.newDescription)
            }
            is ModifyHuntingControlEventIntent.DeleteAttachment -> {
                val index = intent.index
                if (index < event.attachments.size) {
                    val attachment = event.attachments[intent.index].copy(deleted = true)
                    event.copy(attachments = event.attachments.update(index, attachment))
                } else {
                    event
                }
            }
            is ModifyHuntingControlEventIntent.AddAttachment -> {
                event.copy(attachments = viewModel.event.attachments + listOf(intent.newAttachment))
            }
        }

        return createViewModel(
            event = updatedEvent,
            allGameWardens = viewModel.gameWardens,
            selfInspectorWarning = selfInspectorWarning,
        )
    }

    protected fun createViewModel(
        event: HuntingControlEventData,
        allGameWardens: List<HuntingControlGameWarden>,
        selfInspectorWarning: Boolean,
    ): ModifyHuntingControlEventViewModel {

        val gameWardensForEventDate = getGameWardendsForDate(
            date = event.date,
            allWardens = allGameWardens
        )
        var fieldsToBeDisplayed = getFieldsToBeDisplayed(event)
        val validationErrors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = fieldsToBeDisplayed,
            gameWardens = gameWardensForEventDate,
        )
        val eventIsValid = validationErrors.isEmpty()
        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors, selfInspectorWarning)

        return ModifyHuntingControlEventViewModel(
            event = event,
            gameWardens = allGameWardens,
            selfInspectorWarning = selfInspectorWarning,
            fields = fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
                fieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    event = event,
                    gameWardens = gameWardensForEventDate,
                )
            },
            eventIsValid = eventIsValid,
        )
    }

    private fun List<FieldSpecification<HuntingControlEventField>>.injectErrorLabels(
        validationErrors: List<HuntingControlEventValidator.Error>,
        selfInspectorWarning: Boolean,
    ): List<FieldSpecification<HuntingControlEventField>> {
        if (validationErrors.isEmpty() && !selfInspectorWarning) {
            return this
        }

        val result = mutableListOf<FieldSpecification<HuntingControlEventField>>()
        forEach { fieldSpecification ->
            when (fieldSpecification.fieldId.type) {
                HuntingControlEventField.Type.INSPECTORS -> {
                    result.add(fieldSpecification)

                    if (validationErrors.contains(HuntingControlEventValidator.Error.NO_INSPECTORS_FOR_DATE)) {
                        result.add(HuntingControlEventField.Type.ERROR_NO_INSPECTORS_FOR_DATE.noRequirement())
                    }
                }
                HuntingControlEventField.Type.INSPECTOR_NAMES -> {
                    result.add(fieldSpecification)

                    if (selfInspectorWarning) {
                        result.add(HuntingControlEventField.Type.ERROR_NO_SELF_AS_INSPECTOR.noRequirement())
                    }
                }
                HuntingControlEventField.Type.LOCATION,
                HuntingControlEventField.Type.DATE,
                HuntingControlEventField.Type.START_AND_END_TIME,
                HuntingControlEventField.Type.START_TIME,
                HuntingControlEventField.Type.END_TIME,
                HuntingControlEventField.Type.DURATION,
                HuntingControlEventField.Type.EVENT_TYPE,
                HuntingControlEventField.Type.NUMBER_OF_INSPECTORS,
                HuntingControlEventField.Type.COOPERATION,
                HuntingControlEventField.Type.OTHER_PARTICIPANTS,
                HuntingControlEventField.Type.WOLF_TERRITORY,
                HuntingControlEventField.Type.LOCATION_DESCRIPTION,
                HuntingControlEventField.Type.EVENT_DESCRIPTION,
                HuntingControlEventField.Type.ERROR_NO_INSPECTORS_FOR_DATE,
                HuntingControlEventField.Type.ERROR_NO_SELF_AS_INSPECTOR,
                HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS,
                HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS,
                HuntingControlEventField.Type.HEADLINE_ATTACHMENTS,
                HuntingControlEventField.Type.ATTACHMENT,
                HuntingControlEventField.Type.ADD_ATTACHMENT -> {
                    result.add(fieldSpecification)
                }
            }
        }
        return result
    }

    private fun getFieldsToBeDisplayed(
        event: HuntingControlEventData,
    ): List<FieldSpecification<HuntingControlEventField>> {

        return FieldSpecificationListBuilder<HuntingControlEventField>()
            .add(
                HuntingControlEventField.Type.LOCATION.required(),
                HuntingControlEventField.Type.LOCATION_DESCRIPTION.voluntary(),
                HuntingControlEventField.Type.WOLF_TERRITORY.required(),
                HuntingControlEventField.Type.EVENT_TYPE.required(),
                HuntingControlEventField.Type.EVENT_DESCRIPTION.let { field ->
                    if (event.eventType.value == HuntingControlEventType.OTHER) {
                        field.required()
                    } else {
                        field.voluntary()
                    }
                },
                HuntingControlEventField.Type.DATE.required(),
                HuntingControlEventField.Type.START_AND_END_TIME.required(),
                HuntingControlEventField.Type.DURATION.noRequirement(),
                HuntingControlEventField.Type.INSPECTORS.required(),
                HuntingControlEventField.Type.INSPECTOR_NAMES.noRequirement()
                    .takeIf { event.inspectors.isNotEmpty() },
                HuntingControlEventField.Type.NUMBER_OF_INSPECTORS.required(),
                HuntingControlEventField.Type.COOPERATION.required(),
                HuntingControlEventField.Type.OTHER_PARTICIPANTS.voluntary(),
                HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS.required(),
                HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS.required(),
                HuntingControlEventField.Type.HEADLINE_ATTACHMENTS.noRequirement(),
            )
            .add(
                *event.attachments.mapIndexed { index, _ ->
                    HuntingControlEventField.Type.ATTACHMENT.toField(index).voluntary()
                }.toTypedArray()
            )
            .add(
                HuntingControlEventField.Type.ADD_ATTACHMENT.noRequirement(),
            )
            .toList()
    }

    private fun getGameWardendsForDate(
        date: LocalDate?,
        allWardens: List<HuntingControlGameWarden>
    ): List<HuntingControlGameWarden> {
        if (date == null) {
            return listOf()
        }
        return allWardens.filter { warden ->
            val period = LocalDatePeriod(warden.startDate.toPeriodDate(), warden.endDate.toPeriodDate())
            date.isWithinPeriod(period)
        }
    }

    protected fun ModifyHuntingControlEventViewModel.applyPendingIntents(): ModifyHuntingControlEventViewModel {
        var viewModel = this
        pendingIntents.forEach { intent ->
            viewModel = handleIntent(intent, viewModel)
        }
        pendingIntents.clear()

        return viewModel
    }

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.event?.let { event ->
            SavedState(
                eventData = event,
                eventLocationCanBeMovedAutomatically = eventLocationCanBeMovedAutomatically
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredEvent = state.eventData
        eventLocationCanBeMovedAutomatically = state.eventLocationCanBeMovedAutomatically
    }

    @Serializable
    data class SavedState(
        val eventData: HuntingControlEventData,
        val eventLocationCanBeMovedAutomatically: Boolean,
    )

    companion object {
        private val logger by getLogger(ModifyHuntingControlEventController::class)
    }
}

private fun HuntingControlEventField.Type.noRequirement(): FieldSpecification<HuntingControlEventField> {
    return this.toField().noRequirement()
}

private fun HuntingControlEventField.Type.required(
    indicateRequirementStatus: Boolean = true
): FieldSpecification<HuntingControlEventField> {
    return this.toField().required(indicateRequirementStatus)
}

private fun HuntingControlEventField.Type.voluntary(
    indicateRequirementStatus: Boolean = true
): FieldSpecification<HuntingControlEventField> {
    return this.toField().voluntary(indicateRequirementStatus)
}

private fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply {
    this[index] = item
}
