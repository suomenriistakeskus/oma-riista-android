package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.metadata.model.SrvaEventCategory
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.model.*
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.FieldSpecification

internal object CommonSrvaEventValidator {
    enum class Error {
        MISSING_LOCATION,
        MISSING_SPECIES,
        MISSING_OTHER_SPECIES_DESCRIPTION,
        INVALID_SPECIMEN_AMOUNT,
        MISSING_EVENT_CATEGORY,
        INVALID_EVENT_CATEGORY,
        MISSING_DEPORTATION_ORDER_NUMBER,
        MISSING_EVENT_TYPE,
        INVALID_EVENT_TYPE,
        MISSING_EVENT_TYPE_DETAIL,
        INVALID_EVENT_TYPE_DETAIL,
        MISSING_OTHER_EVENT_TYPE_DETAIL_DESCRIPTION,
        MISSING_EVENT_RESULT,
        INVALID_EVENT_RESULT,
        MISSING_EVENT_RESULT_DETAIL,
        INVALID_EVENT_RESULT_DETAIL,
        MISSING_SRVA_METHOD,
        INVALID_SRVA_METHOD,
        INVALID_PERSON_COUNT,
        INVALID_HOURS_SPENT,
    }

    private val logger by getLogger(CommonSrvaEventValidator::class)

    fun validate(
        srvaEvent: CommonSrvaEventData,
        srvaMetadata: SrvaMetadata,
        displayedFields: List<FieldSpecification<SrvaEventField>>,
    ): List<Error> {
        val categoryMetadata = srvaMetadata.getCategory(srvaEvent.eventCategory)

        return displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId.type) {
                SrvaEventField.Type.LOCATION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_LOCATION.takeIf {
                            srvaEvent.location is CommonLocation.Unknown
                        }
                    }
                }
                SrvaEventField.Type.SPECIES_CODE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_SPECIES.takeIf {
                            srvaEvent.species is Species.Unknown
                        }
                    }
                SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_OTHER_SPECIES_DESCRIPTION.takeIf {
                            when (srvaEvent.species) {
                                Species.Other -> srvaEvent.otherSpeciesDescription.isNullOrBlank()
                                is Species.Known,
                                Species.Unknown -> {
                                    logger.w {
                                        "Didn't expect ${fieldSpecification.fieldId.type} to be displayed when " +
                                                "species if ${srvaEvent.species}"
                                    }
                                    false
                                }
                            }
                        }
                    }
                SrvaEventField.Type.SPECIMEN_AMOUNT ->
                    fieldSpecification.ifRequired {
                        val specimenAmount = srvaEvent.specimenAmount ?: 0
                        Error.INVALID_SPECIMEN_AMOUNT.takeIf {
                            specimenAmount == 0 || specimenAmount > SrvaConstants.MAX_SPECIMEN_AMOUNT
                        }
                    }
                SrvaEventField.Type.EVENT_CATEGORY ->
                    when {
                        srvaEvent.eventCategory.rawBackendEnumValue == null ->
                            fieldSpecification.ifRequired { Error.MISSING_EVENT_CATEGORY }
                        categoryMetadata == null -> Error.INVALID_EVENT_CATEGORY
                        else -> null
                    }
                SrvaEventField.Type.DEPORTATION_ORDER_NUMBER ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEPORTATION_ORDER_NUMBER.takeIf {
                            srvaEvent.deportationOrderNumber.isNullOrBlank()
                        }
                    }
                SrvaEventField.Type.EVENT_TYPE ->
                    if (srvaEvent.eventType.rawBackendEnumValue == null) {
                        fieldSpecification.ifRequired { Error.MISSING_EVENT_TYPE }
                    } else if (!categoryMetadata.containsEventType(srvaEvent.eventType)) {
                        Error.INVALID_EVENT_TYPE
                    } else {
                        null
                    }
                SrvaEventField.Type.EVENT_TYPE_DETAIL ->
                    if (srvaEvent.eventTypeDetail.rawBackendEnumValue == null) {
                        fieldSpecification.ifRequired { Error.MISSING_EVENT_TYPE_DETAIL }
                    } else if (!categoryMetadata.validateEventTypeDetail(srvaEvent)) {
                        Error.INVALID_EVENT_TYPE_DETAIL
                    } else {
                        null
                    }
                SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_OTHER_EVENT_TYPE_DETAIL_DESCRIPTION.takeIf {
                            srvaEvent.otherEventTypeDetailDescription.isNullOrBlank()
                        }
                    }
                SrvaEventField.Type.EVENT_RESULT ->
                    if (srvaEvent.eventResult.rawBackendEnumValue == null) {
                        fieldSpecification.ifRequired { Error.MISSING_EVENT_RESULT }
                    } else if (!categoryMetadata.containsEventResult(srvaEvent.eventResult)) {
                        Error.INVALID_EVENT_RESULT
                    } else {
                        null
                    }
                SrvaEventField.Type.EVENT_RESULT_DETAIL ->
                    if (srvaEvent.eventResultDetail.rawBackendEnumValue == null) {
                        fieldSpecification.ifRequired { Error.MISSING_EVENT_RESULT_DETAIL }
                    } else if (!categoryMetadata.validateEventResultDetail(srvaEvent)) {
                        Error.INVALID_EVENT_RESULT_DETAIL
                    } else {
                        null
                    }
                SrvaEventField.Type.METHOD_ITEM -> {
                    val method = srvaEvent.methods.getOrNull(fieldSpecification.fieldId.index)

                    if (method == null || method.type.rawBackendEnumValue == null) {
                        logger.v { "SRVA method null for index ${fieldSpecification.fieldId.index}" }
                        fieldSpecification.ifRequired { Error.MISSING_SRVA_METHOD }
                    } else if (!categoryMetadata.containsSrvaMethod(method)) {
                        logger.v { "Illegal SRVA method for index ${fieldSpecification.fieldId.index}: $method" }
                        Error.INVALID_SRVA_METHOD
                    } else {
                        null
                    }
                }
                SrvaEventField.Type.PERSON_COUNT -> {
                    val personCount = srvaEvent.personCount ?: 0
                    Error.INVALID_PERSON_COUNT.takeIf {
                        personCount > SrvaConstants.MAX_PERSON_COUNT
                    }
                }
                SrvaEventField.Type.HOURS_SPENT -> {
                    val hoursSpent = srvaEvent.hoursSpent ?: 0
                    Error.INVALID_HOURS_SPENT.takeIf {
                        hoursSpent > SrvaConstants.MAX_HOURS_SPENT
                    }
                }
                SrvaEventField.Type.DATE_AND_TIME,
                SrvaEventField.Type.SPECIMEN,
                SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION,
                SrvaEventField.Type.APPROVER_OR_REJECTOR,
                SrvaEventField.Type.METHOD_HEADER,
                SrvaEventField.Type.SELECTED_METHODS,
                SrvaEventField.Type.OTHER_METHOD_DESCRIPTION,
                SrvaEventField.Type.DESCRIPTION -> null
            }
        }.also { errors ->
            if (errors.isEmpty()) {
                logger.v { "SRVA event is valid!" }
            } else {
                logger.d { "SRVA event validation errors: $errors" }
            }
        }
    }
}

private fun SrvaEventCategory?.containsEventType(eventType: BackendEnum<SrvaEventType>): Boolean {
    return when (this) {
        null -> false
        else -> possibleEventTypes.contains(eventType)
    }
}

private fun SrvaEventCategory?.validateEventTypeDetail(srvaEvent: CommonSrvaEventData): Boolean {
    if (this == null) {
        return true
    }

    val possibleDetails = possibleEventTypeDetailsFor(srvaEvent)
    return when {
        possibleDetails.isEmpty() -> srvaEvent.eventTypeDetail.rawBackendEnumValue == null
        else -> possibleDetails.contains(srvaEvent.eventTypeDetail)
    }
}

private fun SrvaEventCategory?.containsEventResult(eventResult: BackendEnum<SrvaEventResult>): Boolean {
    return when (this) {
        null -> false
        else -> possibleEventResults.contains(eventResult)
    }
}

private fun SrvaEventCategory?.validateEventResultDetail(srvaEvent: CommonSrvaEventData): Boolean {
    if (this == null) {
        return true
    }

    val possibleDetails = possibleEventResultDetailsFor(srvaEvent)
    return when {
        possibleDetails.isEmpty() -> srvaEvent.eventResultDetail.rawBackendEnumValue == null
        else -> possibleDetails.contains(srvaEvent.eventResultDetail)
    }
}

private fun SrvaEventCategory?.containsSrvaMethod(method: CommonSrvaMethod): Boolean {
    return when (this) {
        null -> false
        else -> possibleMethods.contains(method.type)
    }
}
