package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlEventType
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.model.*
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.dataField.*
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider

internal class ModifyHuntingControlEventFieldProducer(
    private val stringProvider: StringProvider,
) {
    private val eventTypeFieldFactory = EnumStringListFieldFactory.create<HuntingControlEventType>(stringProvider)
    private val localDateTimeProvider: LocalDateTimeProvider = SystemDateTimeProvider()

    fun createField(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
        gameWardens: List<HuntingControlGameWarden>,
    ) : DataField<HuntingControlEventField>? {
        return when (fieldSpecification.fieldId.type) {
            HuntingControlEventField.Type.EVENT_TYPE -> eventType(fieldSpecification, event)
            HuntingControlEventField.Type.DATE -> date(fieldSpecification, event)
            HuntingControlEventField.Type.START_AND_END_TIME -> startAndEndTime(fieldSpecification, event)
            HuntingControlEventField.Type.DURATION -> duration(fieldSpecification, event)
            HuntingControlEventField.Type.INSPECTORS -> inspectors(fieldSpecification, event, gameWardens)
            HuntingControlEventField.Type.INSPECTOR_NAMES -> inspectorNames(fieldSpecification, event)
            HuntingControlEventField.Type.NUMBER_OF_INSPECTORS -> numberOfInspectors(fieldSpecification, event)
            HuntingControlEventField.Type.COOPERATION -> cooperation(fieldSpecification, event)
            HuntingControlEventField.Type.OTHER_PARTICIPANTS -> otherParticipants(fieldSpecification, event)
            HuntingControlEventField.Type.WOLF_TERRITORY -> wolfTerritory(fieldSpecification, event)
            HuntingControlEventField.Type.LOCATION -> location(fieldSpecification, event)
            HuntingControlEventField.Type.LOCATION_DESCRIPTION -> locationDescription(fieldSpecification, event)
            HuntingControlEventField.Type.EVENT_DESCRIPTION -> eventDescription(fieldSpecification, event)
            HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS -> numberOfCustomers(fieldSpecification, event)
            HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS -> numberOfProofOrders(fieldSpecification, event)
            HuntingControlEventField.Type.ERROR_NO_INSPECTORS_FOR_DATE -> errorNoInspectorsForDate(fieldSpecification)
            HuntingControlEventField.Type.HEADLINE_ATTACHMENTS -> attachmentHeadline(fieldSpecification)
            HuntingControlEventField.Type.ATTACHMENT -> attachment(fieldSpecification, event)
            HuntingControlEventField.Type.ADD_ATTACHMENT -> addAttachment(fieldSpecification)

            // explicitly list unexpected fields, don't use else here!
            HuntingControlEventField.Type.START_TIME,
            HuntingControlEventField.Type.END_TIME -> {
                throw RuntimeException("Was not expecting $fieldSpecification to be displayed")
            }
        }
    }

    private fun eventType(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): StringListField<HuntingControlEventField> {
        return eventTypeFieldFactory.create(
            fieldId = fieldSpecification.fieldId,
            currentEnumValue = event.eventType,
            allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not()
        ) {
            label = stringProvider.getString(RR.string.hunting_control_event_type)
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun date(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): DateField<HuntingControlEventField> {
        return DateField(fieldSpecification.fieldId, event.date) {
            label = stringProvider.getString(RR.string.hunting_control_date)
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            minDate = LocalDate(2020, 1, 1) // TODO
            maxDate = localDateTimeProvider.now().date
        }
    }

    private fun startAndEndTime(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): TimespanField<HuntingControlEventField> {
        return TimespanField(
            id = fieldSpecification.fieldId,
            startTime = event.startTime,
            endTime = event.endTime,
            startFieldId = HuntingControlEventField(HuntingControlEventField.Type.START_TIME),
            endFieldId = HuntingControlEventField(HuntingControlEventField.Type.END_TIME),
        ) {
            startLabel = stringProvider.getString(RR.string.hunting_control_start_time)
            endLabel = stringProvider.getString(RR.string.hunting_control_end_time)
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun inspectors(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
        gameWardens: List<HuntingControlGameWarden>,
    ): StringListField<HuntingControlEventField> {
        val gameWardenStringWithIds = gameWardens.sortedBy { gameWarden ->
            gameWarden.lastName + gameWarden.firstName
        }.map { warden ->
            StringWithId(
                id = warden.remoteId,
                string = "${warden.firstName} ${warden.lastName}"
            )
        }
        val selected = event.inspectors.map { inspector ->
            inspector.id // Use as StringId
        }
        return StringListField(
            id = fieldSpecification.fieldId,
            values = gameWardenStringWithIds,
            selected = selected,
        ) {
            mode = StringListField.Mode.MULTI
            label = stringProvider.getString(RR.string.hunting_control_inspectors)
            multiModeChooseText = stringProvider.getString(RR.string.hunting_control_choose_inspector)
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            externalViewConfiguration = StringListField.ExternalViewConfiguration(
                title = stringProvider.getString(RR.string.hunting_control_inspectors),
                filterEnabled = gameWardenStringWithIds.size >= INSPECTOR_FILTER_LIMIT ,
                filterLabelText = stringProvider.getString(RR.string.hunting_control_inspector_selection_search_by_name),
                filterTextHint = stringProvider.getString(RR.string.hunting_control_inspector_selection_name_hint),
            )
            paddingBottom = if (selected.isEmpty()) {
                Padding.SMALL
            } else {
                Padding.NONE
            }
        }
    }

    private fun inspectorNames(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): ChipField<HuntingControlEventField> {
        val inspectors = event.inspectors.sortedBy { inspector ->
            inspector.lastName + inspector.firstName
        }.map { inspector ->
            StringWithId(
                id = inspector.id,
                string = "${inspector.firstName} ${inspector.lastName}",
            )
        }
        return ChipField(fieldSpecification.fieldId, inspectors) {
            mode = ChipField.Mode.DELETE
            paddingTop = Padding.SMALL
            paddingBottom = Padding.SMALL
        }
    }

    private fun duration(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): StringField<HuntingControlEventField> {
        val hoursAndMinutes = if (event.startTime == null || event.endTime == null) {
            HoursAndMinutes(0)
        } else {
            HoursAndMinutes(event.startTime.minutesUntil(event.endTime))
        }

        return StringField(
            fieldSpecification.fieldId, hoursAndMinutes.formatHoursAndMinutesString(
                stringProvider = stringProvider,
                zeroMinutesStringId = RR.string.hunting_control_duration_zero,
            )
        ) {
            readOnly = true
            singleLine = true
            paddingTop = Padding.SMALL
            paddingBottom = Padding.SMALL
            label = stringProvider.getString(RR.string.hunting_control_duration)
        }
    }

    private fun numberOfInspectors(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): StringField<HuntingControlEventField> {
        return StringField(fieldSpecification.fieldId, event.inspectors.size.toString()) {
            readOnly = true
            singleLine = true
            paddingTop = Padding.SMALL
            paddingBottom = Padding.MEDIUM
            label = stringProvider.getString(RR.string.hunting_control_number_of_inspectors)
        }
    }

    private fun cooperation(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): ChipField<HuntingControlEventField> {
        val values = HuntingControlCooperationType.values().map {
            it.toLocalizedStringWithId(stringProvider)
        }
        val selected = event.cooperationTypes.map { cooperationType ->
            cooperationType.toLocalizedStringWithId(stringProvider).id
        }
        return ChipField(
            id = fieldSpecification.fieldId,
            chips = values,
            selectedIds = selected,
        ) {
            mode = ChipField.Mode.TOGGLE
            paddingTop = Padding.MEDIUM
            paddingBottom = Padding.MEDIUM
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.hunting_control_cooperation_type)
        }
    }

    private fun otherParticipants(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData
    ): StringField<HuntingControlEventField> {
        return StringField(fieldSpecification.fieldId, event.otherParticipants ?: "") {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.hunting_control_other_participants)
        }
    }

    private fun wolfTerritory(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData
    ): BooleanField<HuntingControlEventField> {
        return BooleanField(fieldSpecification.fieldId, event.wolfTerritory) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.hunting_control_wolf_territory)
        }
    }

    private fun location(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): LocationField<HuntingControlEventField> {
        return LocationField(fieldSpecification.fieldId, event.location) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun numberOfCustomers(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): IntField<HuntingControlEventField> {
        return IntField(fieldSpecification.fieldId, event.customerCount) {
            label = stringProvider.getString(RR.string.hunting_control_number_of_customers)
            readOnly = false
            maxValue = 999
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun numberOfProofOrders(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): IntField<HuntingControlEventField> {
        return IntField(fieldSpecification.fieldId, event.proofOrderCount) {
            label = stringProvider.getString(RR.string.hunting_control_number_of_proof_orders)
            readOnly = false
            maxValue = 999
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun locationDescription(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData
    ): StringField<HuntingControlEventField> {
        return StringField(fieldSpecification.fieldId, event.locationDescription ?: "") {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.hunting_control_location_description)
            paddingTop = Padding.SMALL
        }
    }

    private fun eventDescription(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData
    ): StringField<HuntingControlEventField> {
        return StringField(fieldSpecification.fieldId, event.description ?: "") {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.hunting_control_event_description)
        }
    }

    private fun errorNoInspectorsForDate(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
    ): LabelField<HuntingControlEventField> {
        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.hunting_control_error_no_inspectors_for_selected_date),
            type = LabelField.Type.ERROR,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun attachmentHeadline(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
    ): LabelField<HuntingControlEventField> {
        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.hunting_control_attachments),
            type = LabelField.Type.CAPTION
        ) {
            paddingBottom = Padding.SMALL
        }
    }

    private fun attachment(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
        event: HuntingControlEventData,
    ): AttachmentField<HuntingControlEventField>? {
        val index = fieldSpecification.fieldId.index
        if (event.attachments.size > index) {
            val attachment = event.attachments[index]
            if (!attachment.deleted) {
                return AttachmentField(
                    id = HuntingControlEventField(
                        type = fieldSpecification.fieldId.type,
                        index = index
                    ),
                    localId = attachment.localId,
                    filename = attachment.fileName,
                    isImage = attachment.isImage,
                    thumbnailBase64 = attachment.thumbnailBase64,
                ) {
                    readOnly = false
                    requirementStatus = fieldSpecification.requirementStatus
                }
            }
        }
        return null
    }

    private fun addAttachment(
        fieldSpecification: FieldSpecification<HuntingControlEventField>,
    ): ButtonField<HuntingControlEventField> {
        return ButtonField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.hunting_control_add_attachment)
        ) {
            paddingTop = Padding.SMALL
            paddingBottom = Padding.MEDIUM_LARGE
        }
    }

    companion object {
        private const val INSPECTOR_FILTER_LIMIT = 5
    }
}
