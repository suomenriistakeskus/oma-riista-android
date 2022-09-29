package fi.riista.common.domain.huntingControl.ui.modify

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.domain.huntingControl.ui.*
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.FileSaveResult
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.isWithinPeriod
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.*
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ModifyHuntingControlEventController(
    val stringProvider: StringProvider,
    private val commonFileProvider: CommonFileProvider,
) : ControllerWithLoadableModel<ModifyHuntingControlEventViewModel>(),
    IntentHandler<HuntingControlEventIntent>,
    HasUnreproducibleState<ModifyHuntingControlEventController.SavedState> {

    val locationEventDispatcher: HuntingControlEventLocationEventDispatcher =
        HuntingControlEventGeoLocationEventToIntentMapper(intentHandler = this)
    val stringWithIdEventDispatcher: HuntingControlEventStringWithIdEventDispatcher =
        HuntingControlEventStringWithIdEventToIntentMapper(intentHandler = this)
    val stringWithIdClickEventDispatcher: HuntingControlEventStringWithIdClickEventDispatcher =
        HuntingControlEventStringWithIdClickEventToIntentMapper(intentHandler = this)
    val intEventDispatcher: HuntingControlEventIntEventDispatcher =
        HuntingControlEventIntEventToIntentMapper(intentHandler = this)
    val stringEventDispatcher: HuntingControlEventStringEventDispatcher =
        HuntingControlEventStringEventToIntentMapper(intentHandler = this)
    val booleanEventDispatcher: HuntingControlEventBooleanEventDispatcher =
        HuntingControlEventBooleanEventToIntentMapper(intentHandler = this)
    val dateEventDispatcher: HuntingControlEventLocalDateEventDispatcher =
        HuntingControlEventLocalDateEventToIntentMapper(intentHandler = this)
    val timeEventDispatcher: HuntingControlEventLocalTimeEventDispatcher =
        HuntingControlEventLocalTimeEventToIntentMapper(intentHandler = this)
    val attachmentActionEventDispatcher: HuntingControlAttachmentActionEventDispatcher =
        HuntingControlActionEventToIntentMapper(intentHandler = this)
    val addAttachmentEventDispatcher: HuntingControlAttachmentEventDispatcher =
        HuntingControlAttachmentEventToIntentMapper(intentHandler = this)

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
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

    private val pendingIntents = mutableListOf<HuntingControlEventIntent>()

    fun getAttachment(field: HuntingControlEventField): HuntingControlAttachment? {
        val viewModel = getLoadedViewModelOrNull() ?: return null

        if (viewModel.event.attachments.size > field.index) {
            return viewModel.event.attachments[field.index]
        }
        return null
    }

    protected suspend fun moveAttachmentsToAttachmentsDirectory() {
        val srvaEvent = getLoadedViewModelOrNull()?.event ?: kotlin.run {
            return
        }

        srvaEvent.attachments.forEach { attachment ->
            attachment.moveToAttachmentsDirectory()
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

    override fun handleIntent(intent: HuntingControlEventIntent) {
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
        intent: HuntingControlEventIntent,
        viewModel: ModifyHuntingControlEventViewModel,
    ): ModifyHuntingControlEventViewModel {

        val event = viewModel.event

        val updatedEvent = when (intent) {
            is HuntingControlEventIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    eventLocationCanBeMovedAutomatically = false
                }
                event.copy(location = intent.newLocation.asKnownLocation())
            }
            is HuntingControlEventIntent.ChangeEventType -> {
                event.copy(eventType = intent.newEvenType.toBackendEnum())
            }
            is HuntingControlEventIntent.ChangeLocationDescription -> {
                event.copy(locationDescription = intent.newDescription)
            }
            is HuntingControlEventIntent.ChangeNumberOfCustomers -> {
                event.copy(customerCount = intent.numberOfCustomers)
            }
            is HuntingControlEventIntent.ChangeDate -> {
                // Remove inspectors that are not available on the new date
                val availableGameWardens = getGameWardendsForDate(
                    date = event.date,
                    allWardens = viewModel.gameWardens
                )
                event.copy(date = intent.newDate, inspectors = event.inspectors.filter { inspector ->
                    availableGameWardens.firstOrNull { inspector.id == it.remoteId } != null
                })
            }
            is HuntingControlEventIntent.ChangeStartTime -> {
                val endTime = event.endTime
                val startTime = endTime?.let { intent.newStartTime.coerceAtMost(it) } ?: intent.newStartTime
                event.copy(startTime = startTime)
            }
            is HuntingControlEventIntent.ChangeEndTime -> {
                val startTime = event.startTime
                val endTime = startTime?.let { intent.newEndTime.coerceAtLeast(it) } ?: intent.newEndTime
                event.copy(endTime = endTime)
            }
            is HuntingControlEventIntent.ChangeWolfTerritory -> {
                event.copy(wolfTerritory = intent.newWolfTerritory)
            }
            is HuntingControlEventIntent.ChangeOtherPartisipants -> {
                event.copy(otherParticipants = intent.newOtherPartisipants)
            }
            is HuntingControlEventIntent.ChangeInspectors -> {
                val inspectors = intent.newInspectors.mapNotNull { stringWithId ->
                    viewModel.gameWardens.firstOrNull { it.remoteId == stringWithId.id }?.let { warden ->
                        HuntingControlEventInspector(
                            id = warden.remoteId,
                            firstName = warden.firstName,
                            lastName = warden.lastName,
                        )
                    }
                }
                event.copy(inspectors = inspectors)
            }
            is HuntingControlEventIntent.RemoveInspectors -> {
                val inspectors = event.inspectors.filter { eventInspector ->
                    intent.removeInspector.id != eventInspector.id
                }
                event.copy(inspectors = inspectors)
            }
            is HuntingControlEventIntent.ToggleCooperationType -> {
                val toggled = intent.toggledCooperationType.toBackendEnum<HuntingControlCooperationType>()
                if (event.cooperationTypes.contains(toggled)) {
                    event.copy(cooperationTypes = event.cooperationTypes.filter {
                        it != toggled
                    })
                } else {
                    event.copy(cooperationTypes = event.cooperationTypes + listOf(toggled))
                }
            }
            is HuntingControlEventIntent.ChangeNumberOfProofOrders -> {
                event.copy(proofOrderCount = intent.numberOfProofOrders)
            }
            is HuntingControlEventIntent.ChangeDescription -> {
                event.copy(description = intent.newDescription)
            }
            is HuntingControlEventIntent.DeleteAttachment -> {
                val index = intent.index
                if (index < event.attachments.size) {
                    val attachment = event.attachments[intent.index].copy(deleted = true)
                    event.copy(attachments = event.attachments.update(index, attachment))
                } else {
                    event
                }
            }
            is HuntingControlEventIntent.AddAttachment -> {
                event.copy(attachments = viewModel.event.attachments + listOf(intent.newAttachment))
            }
        }

        return createViewModel(
            event = updatedEvent,
            allGameWardens = viewModel.gameWardens,
        )
    }

    protected fun createViewModel(
        event: HuntingControlEventData,
        allGameWardens: List<HuntingControlGameWarden>,
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
        fieldsToBeDisplayed = fieldsToBeDisplayed.injectErrorLabels(validationErrors)

        return ModifyHuntingControlEventViewModel(
            event = event,
            gameWardens = allGameWardens,
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
        validationErrors: List<HuntingControlEventValidator.Error>
    ): List<FieldSpecification<HuntingControlEventField>> {
        if (validationErrors.isEmpty()) {
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
                HuntingControlEventField.Type.LOCATION,
                HuntingControlEventField.Type.DATE,
                HuntingControlEventField.Type.START_AND_END_TIME,
                HuntingControlEventField.Type.START_TIME,
                HuntingControlEventField.Type.END_TIME,
                HuntingControlEventField.Type.DURATION,
                HuntingControlEventField.Type.EVENT_TYPE,
                HuntingControlEventField.Type.NUMBER_OF_INSPECTORS,
                HuntingControlEventField.Type.INSPECTOR_NAMES,
                HuntingControlEventField.Type.COOPERATION,
                HuntingControlEventField.Type.OTHER_PARTICIPANTS,
                HuntingControlEventField.Type.WOLF_TERRITORY,
                HuntingControlEventField.Type.LOCATION_DESCRIPTION,
                HuntingControlEventField.Type.EVENT_DESCRIPTION,
                HuntingControlEventField.Type.ERROR_NO_INSPECTORS_FOR_DATE,
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
            val period = LocalDatePeriod(warden.startDate, warden.endDate)
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
