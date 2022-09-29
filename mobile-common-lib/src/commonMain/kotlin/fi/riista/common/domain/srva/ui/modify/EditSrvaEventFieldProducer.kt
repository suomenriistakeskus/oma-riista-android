@file:Suppress("SpellCheckingInspection")

package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.srva.metadata.model.SrvaEventCategory
import fi.riista.common.domain.srva.model.*
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail
import fi.riista.common.ui.dataField.*
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.toStringOrMissingIndicator

internal class EditSrvaEventFieldProducer(
    private val metadataProvider: MetadataProvider,
    private val stringProvider: StringProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
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

    private val eventCategoryFieldFactory = EnumStringListFieldFactory.create<SrvaEventCategoryType>(stringProvider)
    private val eventTypeFieldFactory = EnumStringListFieldFactory.create<SrvaEventType>(stringProvider)
    private val eventTypeDetailFieldFactory = EnumStringListFieldFactory.create<SrvaEventTypeDetail>(stringProvider)
    private val eventResultFieldFactory = EnumStringListFieldFactory.create<SrvaEventResult>(stringProvider)
    private val eventResultDetailFieldFactory = EnumStringListFieldFactory.create<SrvaEventResultDetail>(stringProvider)

    val selectableSrvaSpecies: SpeciesField.SelectableSpecies =
        SpeciesField.SelectableSpecies.Listed(
            species = metadataProvider.srvaMetadata.species + listOf(Species.Other)
        )

    fun createField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        srvaEvent: CommonSrvaEventData,
    ) : DataField<SrvaEventField>? {
        return when (fieldSpecification.fieldId.type) {
            SrvaEventField.Type.LOCATION ->
                LocationField(fieldSpecification.fieldId, srvaEvent.location) {
                    readOnly = false
                    requirementStatus = fieldSpecification.requirementStatus
                }
            SrvaEventField.Type.SPECIES_CODE ->
                SpeciesField(
                        id = fieldSpecification.fieldId,
                        species = srvaEvent.species,
                        entityImage = srvaEvent.images.primaryImage,
                ) {
                    showEntityImage = true
                    readOnly = false
                    requirementStatus = fieldSpecification.requirementStatus
                    selectableSpecies = selectableSrvaSpecies
                }
            SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION ->
                if (srvaEvent.species is Species.Other) {
                    createStringField(
                        fieldSpecification = fieldSpecification,
                        value = srvaEvent.otherSpeciesDescription ?: "",
                        label = RR.string.srva_event_label_other_species_description,
                    ) {
                    }
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
                    readOnly = false
                    requirementStatus = fieldSpecification.requirementStatus
                    maxDateTime = localDateTimeProvider.now()
                }
            SrvaEventField.Type.SPECIMEN_AMOUNT ->
                createIntField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.specimenAmount,
                    maxValue = SrvaConstants.MAX_SPECIMEN_AMOUNT,
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
                ) {
                    requirementStatus = fieldSpecification.requirementStatus
                    readOnly = false
                }
            SrvaEventField.Type.EVENT_CATEGORY ->
                createEnumChoiceField(
                    fieldSpecification = fieldSpecification,
                    selected = srvaEvent.eventCategory,
                    values = metadataProvider.srvaMetadata.eventCategories.map { it.categoryType },
                    label =  RR.string.srva_event_label_event_category,
                    factory = eventCategoryFieldFactory,
                )
            SrvaEventField.Type.DEPORTATION_ORDER_NUMBER ->
                createStringField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.deportationOrderNumber ?: "",
                    label = RR.string.srva_event_label_deportation_order_number
                )
            SrvaEventField.Type.EVENT_TYPE ->
                createEnumChoiceField(
                    fieldSpecification = fieldSpecification,
                    selected = srvaEvent.eventType,
                    values = srvaEvent.categoryMetadata?.possibleEventTypes ?: listOf(),
                    label =  RR.string.srva_event_label_event_type,
                    factory = eventTypeFieldFactory,
                )
            SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION ->
                createStringField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.otherEventTypeDescription ?: "",
                    label = RR.string.srva_event_label_other_event_type_description
                )
            SrvaEventField.Type.EVENT_TYPE_DETAIL ->
                createEnumChoiceField(
                    fieldSpecification = fieldSpecification,
                    selected = srvaEvent.eventTypeDetail,
                    values = srvaEvent.categoryMetadata?.possibleEventTypeDetailsFor(srvaEvent) ?: listOf(),
                    label =  RR.string.srva_event_label_event_type_detail,
                    factory = eventTypeDetailFieldFactory,
                )
            SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION ->
                createStringField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.otherEventTypeDetailDescription ?: "",
                    label = RR.string.srva_event_label_other_event_type_detail_description
                )
            SrvaEventField.Type.EVENT_RESULT ->
                createEnumChoiceField(
                    fieldSpecification = fieldSpecification,
                    selected = srvaEvent.eventResult,
                    values = srvaEvent.categoryMetadata?.possibleEventResults ?: listOf(),
                    label =  RR.string.srva_event_label_event_result,
                    factory = eventResultFieldFactory,
                )
            SrvaEventField.Type.EVENT_RESULT_DETAIL ->
                createEnumChoiceField(
                    fieldSpecification = fieldSpecification,
                    selected = srvaEvent.eventResultDetail,
                    values = srvaEvent.categoryMetadata?.possibleEventResultDetailsFor(srvaEvent) ?: listOf(),
                    label =  RR.string.srva_event_label_event_result_detail,
                    factory = eventResultDetailFieldFactory,
                )
            SrvaEventField.Type.METHOD_HEADER ->
                LabelField(
                    id = fieldSpecification.fieldId,
                    text = stringProvider.getString(RR.string.srva_event_label_method),
                    type = LabelField.Type.CAPTION
                ) {
                    paddingBottom = Padding.NONE
                }
            SrvaEventField.Type.METHOD_ITEM -> {
                srvaEvent.methods.getOrNull(fieldSpecification.fieldId.index)
                    ?.let { method ->
                        BooleanField(
                            id = fieldSpecification.fieldId,
                            value = method.selected,
                        ) {
                            label = method.type.localized(stringProvider)
                            readOnly = false
                            requirementStatus = fieldSpecification.requirementStatus
                            appearance = BooleanField.Appearance.CHECKBOX
                            paddingTop = Padding.SMALL
                            paddingBottom = Padding.SMALL
                        }
                    }
            }
            SrvaEventField.Type.OTHER_METHOD_DESCRIPTION ->
                createStringField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.otherMethodDescription ?: "",
                    label = RR.string.srva_event_label_other_method_description
                ) {
                    paddingTop = Padding.NONE
                }
            SrvaEventField.Type.PERSON_COUNT ->
                createIntField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.personCount,
                    maxValue = SrvaConstants.MAX_PERSON_COUNT,
                    label = RR.string.srva_event_label_person_count
                )
            SrvaEventField.Type.HOURS_SPENT ->
                createIntField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.hoursSpent,
                    maxValue = SrvaConstants.MAX_HOURS_SPENT,
                    label = RR.string.srva_event_label_hours_spent
                )
            SrvaEventField.Type.DESCRIPTION ->
                createStringField(
                    fieldSpecification = fieldSpecification,
                    value = srvaEvent.description ?: "",
                    label = RR.string.srva_event_label_description
                )
            SrvaEventField.Type.SELECTED_METHODS -> {
                logger.d { "Unexpected field to be produced $fieldSpecification" }
                null
            }
        }
    }

    private val CommonSrvaEventData.categoryMetadata: SrvaEventCategory?
        get() = metadataProvider.srvaMetadata.getCategory(eventCategory)

    private fun createStringField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        value: String,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<SrvaEventField> {
        return StringField(fieldSpecification.fieldId, value) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    private fun createIntField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        value: Int?,
        label: RR.string,
        maxValue: Int,
        configureSettings: (IntField.DefaultIntFieldSettings.() -> Unit)? = null
    ): IntField<SrvaEventField> {
        return IntField(fieldSpecification.fieldId, value) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)
            this.maxValue = maxValue

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    private fun <E> createEnumChoiceField(
        fieldSpecification: FieldSpecification<SrvaEventField>,
        selected: BackendEnum<E>,
        values: List<BackendEnum<E>>,
        label: RR.string,
        factory: EnumStringListFieldFactory<E>,
        configureSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null
    ): StringListField<SrvaEventField> where E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
        return factory.create(
            fieldId = fieldSpecification.fieldId,
            currentEnumValue = selected,
            enumValues = values,
            allowEmptyValue = !fieldSpecification.requirementStatus.isRequired(),
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL_MEDIUM

            configureSettings?.let { configure ->
                this.configure()
            }
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

    companion object {
        private val logger by getLogger(EditSrvaEventFieldProducer::class)
    }
}
