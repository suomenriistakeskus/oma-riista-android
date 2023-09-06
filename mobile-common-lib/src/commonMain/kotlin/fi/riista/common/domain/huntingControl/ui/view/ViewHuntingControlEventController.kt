package fi.riista.common.domain.huntingControl.ui.view

import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField.Type
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.logging.getLogger
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.formatHoursAndMinutesString
import fi.riista.common.model.minutesUntil
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.AttachmentField
import fi.riista.common.ui.dataField.ChipField
import fi.riista.common.ui.dataField.DataFieldProducer
import fi.riista.common.ui.dataField.DataFieldProducerProxy
import fi.riista.common.ui.dataField.DateField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.TimespanField
import fi.riista.common.util.toStringOrMissingIndicator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ViewHuntingControlEventController(
    private val huntingControlContext: HuntingControlContext,
    private val huntingControlEventTarget: HuntingControlEventTarget,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewHuntingControlEventViewModel>() {

    private lateinit var dataFieldProducers: List<DataFieldProducer<HuntingControlEvent, HuntingControlEventField>>
    private lateinit var attachmentDataFieldProducer: DataFieldProducer<AttachmentWrapper, HuntingControlEventField>

    init {
        initializeFieldProducers()
    }

    /**
     * Loads the [HuntingControlEvent] and updates the [viewModelLoadStatus] accordingly.
     */
    suspend fun loadHuntingControlEvent() {
        val loadFlow = createLoadViewModelFlow(refresh = false)

        loadFlow.collect { viewModelLoadStatus ->
            updateViewModel(viewModelLoadStatus)
        }
    }

    fun getAttachment(field: HuntingControlEventField): HuntingControlAttachment? {
        val viewModel = getLoadedViewModelOrNull() ?: return null

        if (viewModel.huntingControlEvent.attachments.size > field.index) {
            return viewModel.huntingControlEvent.attachments[field.index]
        }
        return null
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ViewHuntingControlEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val huntingControlRhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = huntingControlEventTarget,
        ) ?: kotlin.run {
            logger.w { "Failed to load a context for the RHY ${huntingControlEventTarget.rhyId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val event = huntingControlRhyContext.findHuntingControlEvent(
            identifiesHuntingControlEvent = huntingControlEventTarget,
        )

        if (event != null) {
            emit(
                ViewModelLoadStatus.Loaded(
                    viewModel = createViewModel(event)
                )
            )
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        huntingControlEvent: HuntingControlEvent,
    ): ViewHuntingControlEventViewModel {
        return ViewHuntingControlEventViewModel(
            huntingControlEvent = huntingControlEvent,
            fields = dataFieldProducers.mapNotNull { producer ->
                producer.produceDataField(huntingControlEvent)
            } + huntingControlEvent.attachments
                .mapIndexedNotNull { index, attachment ->
                    attachmentDataFieldProducer.produceDataField(AttachmentWrapper(index, attachment))
                },
            canEditHuntingControlEvent = huntingControlEvent.canEdit,
        )
    }

    private fun initializeFieldProducers() {
        val fields = listOf(
            { event: HuntingControlEvent ->
                LocationField(
                    id = HuntingControlEventField(Type.LOCATION),
                    location = event.geoLocation.asKnownLocation(),
                ) {
                    readOnly = true
                }
            },
            { event: HuntingControlEvent ->
                event.locationDescription
                    .createValueField(
                        fieldType = Type.LOCATION_DESCRIPTION,
                        label = RR.string.hunting_control_location_description
                    )
            },
            { event: HuntingControlEvent ->
                when (event.wolfTerritory) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                }
                    .let { stringProvider.getString(it) }
                    .createValueField(
                        fieldType = Type.WOLF_TERRITORY,
                        label = RR.string.hunting_control_wolf_territory
                    )
            },
            { event: HuntingControlEvent ->
                event.eventType.localized(stringProvider)
                    .createValueField(
                        fieldType = Type.EVENT_TYPE,
                        label = RR.string.hunting_control_event_type
                    )
            },
            { event: HuntingControlEvent ->
                event.description
                    .createValueField(
                        fieldType = Type.EVENT_DESCRIPTION,
                        label = RR.string.hunting_control_event_description
                    )
            },
            { event: HuntingControlEvent ->
                DateField(HuntingControlEventField(Type.DATE), event.date) {
                    readOnly = true
                    paddingTop = Padding.SMALL
                    paddingBottom = Padding.SMALL
                }
            },
            { event: HuntingControlEvent ->
                TimespanField(
                    id = HuntingControlEventField(Type.START_AND_END_TIME),
                    startTime = event.startTime,
                    endTime = event.endTime,
                    startFieldId = HuntingControlEventField(Type.START_TIME),
                    endFieldId = HuntingControlEventField(Type.END_TIME),
                ) {
                    readOnly = true
                    startLabel = stringProvider.getString(RR.string.hunting_control_start_time)
                    endLabel = stringProvider.getString(RR.string.hunting_control_end_time)
                    paddingTop = Padding.SMALL
                    paddingBottom = Padding.SMALL
                }
            },
            { event: HuntingControlEvent ->
                HoursAndMinutes(minutes = event.startTime.minutesUntil(event.endTime))
                    .formatHoursAndMinutesString(
                        stringProvider = stringProvider,
                        zeroMinutesStringId = RR.string.hunting_control_duration_zero,
                    )
                    .createValueField(
                        fieldType = Type.DURATION,
                        label = RR.string.hunting_control_duration
                    )
            },
            { event: HuntingControlEvent ->
                event.inspectors
                    .sortedBy { inspector ->
                        inspector.lastName + inspector.firstName
                    }
                    .joinToString("\n") { inspector -> "${inspector.firstName} ${inspector.lastName}" }
                    .createValueField(
                        fieldType = Type.INSPECTORS,
                        label = RR.string.hunting_control_inspectors
                    )
            },
            { event: HuntingControlEvent ->
                event.inspectors.size
                    .createValueField(
                        fieldType = Type.NUMBER_OF_INSPECTORS,
                        label = RR.string.hunting_control_number_of_inspectors
                    )
            },
            { event: HuntingControlEvent ->
                val cooperationTypes = event.cooperationTypes.map { type ->
                    type.toLocalizedStringWithId(stringProvider)
                }
                ChipField(HuntingControlEventField(Type.COOPERATION), cooperationTypes) {
                    mode = ChipField.Mode.VIEW
                    label = stringProvider.getString(RR.string.hunting_control_cooperation_type)
                    paddingTop = Padding.SMALL
                    paddingBottom = Padding.SMALL
                }
            },
            { event: HuntingControlEvent ->
                event.otherParticipants
                    .createValueField(
                        fieldType = Type.OTHER_PARTICIPANTS,
                        label = RR.string.hunting_control_other_participants
                    )
            },
            { event: HuntingControlEvent ->
                event.customerCount
                    .createValueField(
                        fieldType = Type.NUMBER_OF_CUSTOMERS,
                        label = RR.string.hunting_control_number_of_customers
                    )
            },
            { event: HuntingControlEvent ->
                event.proofOrderCount
                    .createValueField(
                        fieldType = Type.NUMBER_OF_PROOF_ORDERS,
                        label = RR.string.hunting_control_number_of_proof_orders
                    )
            },
            {
                LabelField(
                    HuntingControlEventField(Type.HEADLINE_ATTACHMENTS),
                    stringProvider.getString(RR.string.hunting_control_attachments),
                    LabelField.Type.CAPTION
                ) {
                    paddingTop = Padding.SMALL
                    paddingBottom = Padding.SMALL
                    allCaps = true
                }
            }
        )

        dataFieldProducers = fields.map { DataFieldProducerProxy(it) }

        val attachmentField =
            { wrapper: AttachmentWrapper ->
                val localId = wrapper.attachment.localId
                if (localId != null && !wrapper.attachment.deleted) {
                    AttachmentField(
                        id = HuntingControlEventField(
                            type = Type.ATTACHMENT,
                            index = wrapper.index
                        ),
                        localId = localId,
                        filename = wrapper.attachment.fileName,
                        isImage = wrapper.attachment.isImage,
                        thumbnailBase64 = wrapper.attachment.thumbnailBase64,
                    ) {
                        readOnly = true
                    }
                } else {
                    null
                }
            }
        attachmentDataFieldProducer = DataFieldProducerProxy(attachmentField)
    }

    private fun Any?.createValueField(
        fieldType: Type,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<HuntingControlEventField> {
        val value = this.toStringOrMissingIndicator()

        return StringField(HuntingControlEventField(fieldType), value) {
            readOnly = true
            singleLine = true
            this.label = stringProvider.getString(label)
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    companion object {
        private val logger by getLogger(ViewHuntingControlEventController::class)
    }

    private class AttachmentWrapper(val index: Int, val attachment: HuntingControlAttachment)
}
