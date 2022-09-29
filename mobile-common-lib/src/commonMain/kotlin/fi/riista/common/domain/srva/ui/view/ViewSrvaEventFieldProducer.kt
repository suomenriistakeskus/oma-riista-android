package fi.riista.common.domain.srva.ui.view

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.dataField.*
import fi.riista.common.util.toStringOrMissingIndicator

internal class ViewSrvaEventFieldProducer(
    private val stringProvider: StringProvider,
) {
    private val specimenFieldSpecifications = listOf(
        SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.GENDER,
            label = stringProvider.getString(RR.string.gender_label),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.AGE,
            label = stringProvider.getString(RR.string.age_label),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
    )

    fun createField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        srvaEvent: CommonSrvaEventData,
    ) : DataField<SrvaEventField>? {
        return when (fieldSpecification.fieldId.type) {
            SrvaEventField.Type.LOCATION ->
                LocationField(fieldSpecification.fieldId, srvaEvent.location) {
                    readOnly = true
                }
            SrvaEventField.Type.SPECIES_CODE ->
                SpeciesField(
                        id = fieldSpecification.fieldId,
                        species = srvaEvent.species,
                        entityImage = srvaEvent.images.primaryImage,
                ) {
                    showEntityImage = true
                }
            SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION ->
                if (srvaEvent.species is Species.Other) {
                    srvaEvent.otherSpeciesDescription
                        .createValueField(
                            fieldSpecification = fieldSpecification,
                            label = RR.string.srva_event_label_other_species_description,
                        )
                } else {
                    null
                }
            SrvaEventField.Type.APPROVER_OR_REJECTOR -> {
                val label = when (srvaEvent.state.value) {
                    SrvaEventState.APPROVED -> RR.string.srva_event_label_approver
                    SrvaEventState.REJECTED -> RR.string.srva_event_label_rejector
                    else -> {
                        return null
                    }
                }

                srvaEvent.approver
                    ?.let {
                        val firstName = if (it.firstName.isNullOrBlank()) {
                            ""
                        } else {
                            "${it.firstName} " // intentionally add space
                        }
                        "$firstName${it.lastName}"
                    }
                    .createValueField(fieldSpecification, label)
            }
            SrvaEventField.Type.DATE_AND_TIME ->
                DateAndTimeField(fieldSpecification.fieldId, srvaEvent.pointOfTime) {
                    readOnly = true
                }
            SrvaEventField.Type.SPECIMEN_AMOUNT ->
                srvaEvent.specimens.count()
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_specimen_amount
                    )
            SrvaEventField.Type.SPECIMEN ->
                SpecimenField(
                    id = fieldSpecification.fieldId,
                    specimenData = SpecimenFieldDataContainer.createForSrva(
                        species = srvaEvent.species,
                        specimens = srvaEvent.specimens,
                        fieldSpecifications = specimenFieldSpecifications,
                    )
                )
            SrvaEventField.Type.EVENT_CATEGORY ->
                srvaEvent.eventCategory.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_event_category
                    )
            SrvaEventField.Type.DEPORTATION_ORDER_NUMBER ->
                srvaEvent.deportationOrderNumber
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_deportation_order_number
                    )
            SrvaEventField.Type.EVENT_TYPE ->
                srvaEvent.eventType.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_event_type
                    )
            SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION ->
                srvaEvent.otherEventTypeDescription
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_other_event_type_description
                    )
            SrvaEventField.Type.EVENT_TYPE_DETAIL ->
                srvaEvent.eventTypeDetail.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_event_type_detail
                    )
            SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION ->
                srvaEvent.otherEventTypeDetailDescription
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_other_event_type_detail_description
                    )
            SrvaEventField.Type.EVENT_RESULT ->
                srvaEvent.eventResult.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_event_result
                    )
            SrvaEventField.Type.EVENT_RESULT_DETAIL ->
                srvaEvent.eventResultDetail.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_event_result_detail
                    )
            SrvaEventField.Type.SELECTED_METHODS ->
                srvaEvent.selectedMethods
                    .joinToString(separator = "\n") { it.localized(stringProvider) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_method
                    )
            SrvaEventField.Type.OTHER_METHOD_DESCRIPTION ->
                srvaEvent.otherMethodDescription
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_other_method_description
                    )
            SrvaEventField.Type.PERSON_COUNT ->
                srvaEvent.personCount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_person_count
                    )
            SrvaEventField.Type.HOURS_SPENT ->
                srvaEvent.hoursSpent
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_hours_spent
                    )
            SrvaEventField.Type.DESCRIPTION ->
                srvaEvent.description
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.srva_event_label_description
                    )
            // should not be displaying single items
            SrvaEventField.Type.METHOD_ITEM,
            SrvaEventField.Type.METHOD_HEADER -> null
        }
    }

    private fun Any?.createValueField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<SrvaEventField> {
        val value = this.toStringOrMissingIndicator()

        return StringField(fieldSpecification.fieldId, value) {
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
}