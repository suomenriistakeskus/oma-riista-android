package fi.riista.common.domain.srva.ui

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.toBackendEnum
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary

internal class SrvaEventFields(
    private val metadataProvider: MetadataProvider,
) {

    /**
     * The context based on which the specifications for [CommonSrvaEventData] fields are determined.
     */
    data class Context(
        val srvaEvent: CommonSrvaEventData,
        val mode: Mode
    ) {
        enum class Mode {
            VIEW,
            EDIT,
        }
    }

    fun getFieldsToBeDisplayed(context: Context): List<FieldSpecification<SrvaEventField>> {
        val safeSpecVersion = context.srvaEvent.srvaSpecVersion.coerceAtMost(Constants.SRVA_SPEC_VERSION)
        return if (safeSpecVersion == 1) {
            getFieldsToBeDisplayedSpecVersion1(context)
        } else {
            // spec version 2 + future versions
            getFieldsToBeDisplayedSpecVersion2(context)
        }
    }


    // Spec version 2

    private fun getFieldsToBeDisplayedSpecVersion2(context: Context): List<FieldSpecification<SrvaEventField>> {
        return when (context.mode) {
            Context.Mode.VIEW -> getFieldsToBeDisplayedForViewingSpecVersion2(context)
            Context.Mode.EDIT -> getFieldsToBeDisplayedForEditingSpecVersion2(context)
        }
    }

    private fun getFieldsToBeDisplayedForViewingSpecVersion2(context: Context): List<FieldSpecification<SrvaEventField>> {
        val srvaEvent = context.srvaEvent
        val srvaCategory = metadataProvider.srvaMetadata.getCategory(
            type = srvaEvent.eventCategory
        )

        return FieldSpecificationListBuilder<SrvaEventField>()
            .add(
                SrvaEventField.Type.LOCATION.noRequirement(),
                SrvaEventField.Type.SPECIES_CODE.noRequirement(),
                SrvaEventField.Type.DATE_AND_TIME.noRequirement(),
                SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.noRequirement()
                    .takeIf { srvaEvent.species is Species.Other },
                SrvaEventField.Type.APPROVER_OR_REJECTOR.noRequirement()
                    .takeIf { context.srvaEvent.approvedOrRejected() },
                SrvaEventField.Type.SPECIMEN_AMOUNT.noRequirement(),
                SrvaEventField.Type.SPECIMEN.noRequirement(),
                SrvaEventField.Type.EVENT_CATEGORY.noRequirement(),
                SrvaEventField.Type.DEPORTATION_ORDER_NUMBER.noRequirement()
                    .takeIf { srvaEvent.eventCategory == SrvaEventCategoryType.DEPORTATION.toBackendEnum() },
                SrvaEventField.Type.EVENT_TYPE.noRequirement(),
                SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.noRequirement()
                    .takeIf { srvaEvent.eventType.value == SrvaEventType.OTHER },
                SrvaEventField.Type.EVENT_TYPE_DETAIL.noRequirement()
                    .takeIf { srvaCategory?.containsEventTypeDetailsFor(srvaEvent) == true },
                SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION.noRequirement()
                    .takeIf {
                        srvaCategory?.containsEventTypeDetailsFor(srvaEvent) == true &&
                                srvaEvent.eventTypeDetail == SrvaEventTypeDetail.OTHER.toBackendEnum()
                    },
                SrvaEventField.Type.EVENT_RESULT.noRequirement(),
                SrvaEventField.Type.EVENT_RESULT_DETAIL.noRequirement()
                    .takeIf { srvaCategory?.containsEventResultDetailsFor(srvaEvent) == true },
                SrvaEventField.Type.SELECTED_METHODS.noRequirement(),
                SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.noRequirement()
                    .takeIf {
                        srvaEvent.selectedMethods.contains(
                            SrvaMethodType.OTHER.toBackendEnum()
                        )
                    },
                SrvaEventField.Type.PERSON_COUNT.noRequirement(),
                SrvaEventField.Type.HOURS_SPENT.noRequirement(),
                SrvaEventField.Type.DESCRIPTION.noRequirement(),
            )
            .toList()
    }

    private fun getFieldsToBeDisplayedForEditingSpecVersion2(context: Context): List<FieldSpecification<SrvaEventField>> {
        val srvaEvent = context.srvaEvent
        val srvaCategory = metadataProvider.srvaMetadata.getCategory(
            type = srvaEvent.eventCategory
        )
        val knownCategorySelected = srvaCategory != null

        return FieldSpecificationListBuilder<SrvaEventField>()
            .add(
                SrvaEventField.Type.LOCATION.required(),
                SrvaEventField.Type.SPECIES_CODE.required(),
                SrvaEventField.Type.DATE_AND_TIME.required(),
                SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.required()
                    .takeIf { srvaEvent.species is Species.Other },
                SrvaEventField.Type.APPROVER_OR_REJECTOR.noRequirement()
                    .takeIf { context.srvaEvent.approvedOrRejected() },
                SrvaEventField.Type.SPECIMEN_AMOUNT.required(),
                SrvaEventField.Type.SPECIMEN.required(),
                SrvaEventField.Type.EVENT_CATEGORY.required(),
                SrvaEventField.Type.DEPORTATION_ORDER_NUMBER.voluntary()
                    .takeIf { srvaEvent.eventCategory == SrvaEventCategoryType.DEPORTATION.toBackendEnum() },
                SrvaEventField.Type.EVENT_TYPE.required().takeIf { knownCategorySelected },
                SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.voluntary()
                    .takeIf { knownCategorySelected && srvaEvent.eventType.value == SrvaEventType.OTHER },
                SrvaEventField.Type.EVENT_TYPE_DETAIL.required()
                    .takeIf { srvaCategory?.containsEventTypeDetailsFor(srvaEvent) == true },
                SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION.voluntary()
                    .takeIf {
                        srvaCategory?.containsEventTypeDetailsFor(srvaEvent) == true &&
                                srvaEvent.eventTypeDetail == SrvaEventTypeDetail.OTHER.toBackendEnum()
                            },
                SrvaEventField.Type.EVENT_RESULT.required().takeIf { knownCategorySelected },
                SrvaEventField.Type.EVENT_RESULT_DETAIL.required()
                    .takeIf { srvaCategory?.containsEventResultDetailsFor(srvaEvent) == true },

                SrvaEventField.Type.METHOD_HEADER.voluntary().takeIf { knownCategorySelected },
                *(srvaEvent.methods.indices.mapNotNull { methodIndex ->
                    if (knownCategorySelected) {
                        SrvaEventField(
                            type = SrvaEventField.Type.METHOD_ITEM,
                            index = methodIndex
                        ).voluntary()
                    } else {
                        null
                    }
                }.toTypedArray()),
                SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.voluntary()
                    .takeIf {
                        knownCategorySelected && srvaEvent.selectedMethods.contains(
                            SrvaMethodType.OTHER.toBackendEnum()
                        )
                    },

                SrvaEventField.Type.PERSON_COUNT.voluntary(),
                SrvaEventField.Type.HOURS_SPENT.voluntary(),
                SrvaEventField.Type.DESCRIPTION.voluntary(),
            )
            .toList()
    }


    // Spec version 1

    private fun getFieldsToBeDisplayedSpecVersion1(context: Context): List<FieldSpecification<SrvaEventField>> {
        return when (context.mode) {
            Context.Mode.VIEW -> getFieldsToBeDisplayedForViewingSpecVersion1(context)
            Context.Mode.EDIT -> getFieldsToBeDisplayedForEditingSpecVersion1(context)
        }
    }

    private fun getFieldsToBeDisplayedForViewingSpecVersion1(context: Context): List<FieldSpecification<SrvaEventField>> {
        return FieldSpecificationListBuilder<SrvaEventField>()
            .add(
                SrvaEventField.Type.LOCATION.noRequirement(),
                SrvaEventField.Type.SPECIES_CODE.noRequirement(),
                SrvaEventField.Type.DATE_AND_TIME.noRequirement(),
                SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.noRequirement()
                    .takeIf { context.srvaEvent.species is Species.Other },
                SrvaEventField.Type.APPROVER_OR_REJECTOR.noRequirement()
                    .takeIf { context.srvaEvent.approvedOrRejected() },
                SrvaEventField.Type.SPECIMEN_AMOUNT.noRequirement(),
                SrvaEventField.Type.SPECIMEN.noRequirement(),
                SrvaEventField.Type.EVENT_CATEGORY.noRequirement(),
                SrvaEventField.Type.EVENT_TYPE.noRequirement(),
                SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.noRequirement()
                    .takeIf { context.srvaEvent.eventType.value == SrvaEventType.OTHER },
                SrvaEventField.Type.EVENT_RESULT.noRequirement(),
                SrvaEventField.Type.SELECTED_METHODS.noRequirement(),
                SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.noRequirement()
                    .takeIf {
                        context.srvaEvent.selectedMethods.contains(
                            SrvaMethodType.OTHER.toBackendEnum()
                        )
                    },
                SrvaEventField.Type.PERSON_COUNT.noRequirement(),
                SrvaEventField.Type.HOURS_SPENT.noRequirement(),
                SrvaEventField.Type.DESCRIPTION.noRequirement(),
            )
            .toList()
    }

    private fun getFieldsToBeDisplayedForEditingSpecVersion1(context: Context): List<FieldSpecification<SrvaEventField>> {
        val knownCategorySelected = metadataProvider.srvaMetadata.hasCategory(
            type = context.srvaEvent.eventCategory
        )

        return FieldSpecificationListBuilder<SrvaEventField>()
            .add(
                SrvaEventField.Type.LOCATION.required(),
                SrvaEventField.Type.SPECIES_CODE.required(),
                SrvaEventField.Type.DATE_AND_TIME.required(),
                SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.required()
                    .takeIf { context.srvaEvent.species is Species.Other },
                SrvaEventField.Type.APPROVER_OR_REJECTOR.noRequirement()
                    .takeIf { context.srvaEvent.approvedOrRejected() },
                SrvaEventField.Type.SPECIMEN_AMOUNT.required(),
                SrvaEventField.Type.SPECIMEN.required(),
                SrvaEventField.Type.EVENT_CATEGORY.required(),
                SrvaEventField.Type.EVENT_TYPE.required().takeIf { knownCategorySelected },
                SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.voluntary()
                    .takeIf { knownCategorySelected && context.srvaEvent.eventType.value == SrvaEventType.OTHER },
                SrvaEventField.Type.EVENT_RESULT.required().takeIf { knownCategorySelected },

                SrvaEventField.Type.METHOD_HEADER.voluntary().takeIf { knownCategorySelected },
                *(context.srvaEvent.methods.indices.mapNotNull { methodIndex ->
                    if (knownCategorySelected) {
                        SrvaEventField(
                            type = SrvaEventField.Type.METHOD_ITEM,
                            index = methodIndex
                        ).voluntary()
                    } else {
                        null
                    }
                }.toTypedArray()),
                SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.voluntary()
                    .takeIf {
                        knownCategorySelected && context.srvaEvent.selectedMethods.contains(
                            SrvaMethodType.OTHER.toBackendEnum()
                        )
                    },

                SrvaEventField.Type.PERSON_COUNT.voluntary(),
                SrvaEventField.Type.HOURS_SPENT.voluntary(),
                SrvaEventField.Type.DESCRIPTION.voluntary(),
            )
            .toList()
    }
}

internal fun SrvaEventField.Type.noRequirement(): FieldSpecification<SrvaEventField> {
    return this.toField().noRequirement()
}

internal fun SrvaEventField.Type.required(
    indicateRequirementStatus: Boolean = true
): FieldSpecification<SrvaEventField> {
    return this.toField().required(indicateRequirementStatus)
}

internal fun SrvaEventField.Type.voluntary(
    indicateRequirementStatus: Boolean = true
): FieldSpecification<SrvaEventField> {
    return this.toField().voluntary(indicateRequirementStatus)
}