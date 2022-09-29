package fi.riista.common.domain.srva.metadata.model

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.model.BackendEnum
import fi.riista.common.domain.srva.model.CommonSrvaTypeDetail
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail


data class SrvaEventCategory(
    val categoryType: BackendEnum<SrvaEventCategoryType>,
    val possibleEventTypes: List<BackendEnum<SrvaEventType>>,
    val possibleEventTypeDetails: Map<BackendEnum<SrvaEventType>, List<CommonSrvaTypeDetail>>,
    val possibleEventResults: List<BackendEnum<SrvaEventResult>>,
    val possibleEventResultDetails: Map<BackendEnum<SrvaEventResult>, List<BackendEnum<SrvaEventResultDetail>>>,
    val possibleMethods: List<BackendEnum<SrvaMethodType>>,
) {
    internal fun possibleEventTypeDetailsFor(srvaEvent: CommonSrvaEventData): List<BackendEnum<SrvaEventTypeDetail>> {
        val typeDetails = possibleEventTypeDetails[srvaEvent.eventType] ?: return listOf()

        return when (srvaEvent.species) {
            is Species.Known -> typeDetails.filter { it.isApplicableFor(speciesCode = srvaEvent.species.speciesCode) }
            Species.Other -> typeDetails
            Species.Unknown -> typeDetails.filter { it.speciesCodes.isEmpty() }
        }.map { it.detailType }
    }

    internal fun containsEventTypeDetailsFor(srvaEvent: CommonSrvaEventData): Boolean =
        possibleEventTypeDetailsFor(srvaEvent).isNotEmpty()

    internal fun possibleEventResultDetailsFor(srvaEvent: CommonSrvaEventData) =
        possibleEventResultDetails[srvaEvent.eventResult] ?: listOf()

    internal fun containsEventResultDetailsFor(srvaEvent: CommonSrvaEventData): Boolean =
        possibleEventResultDetailsFor(srvaEvent).isNotEmpty()
}
