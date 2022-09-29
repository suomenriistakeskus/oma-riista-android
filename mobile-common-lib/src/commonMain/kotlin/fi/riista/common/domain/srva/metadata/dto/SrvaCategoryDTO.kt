package fi.riista.common.domain.srva.metadata.dto

import fi.riista.common.domain.srva.metadata.model.SrvaEventCategory
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.model.toBackendEnum
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A DTO for srva category (not for sending category information due to ignored fields)
 */
@Serializable
data class SrvaCategoryDTO(
    @SerialName("name")
    val category: String,
    val types: List<String>,
    // spec version 2: details for certain types. Key: type, value: list of possible details
    val typeDetails: Map<String, List<SrvaTypeDetailDTO>>? = null,
    val results: List<String>,
    // spec version 2: details for certain results. Key: result, value: list of possible details
    val resultDetails: Map<String, List<String>>? = null,
    val methods: List<SrvaMethodDTO>,
)

internal fun SrvaCategoryDTO.toSrvaEventCategory(): SrvaEventCategory {
    return SrvaEventCategory(
        categoryType = category.toBackendEnum(),
        possibleEventTypes = types.map { it.toBackendEnum() },
        possibleEventTypeDetails = typeDetails?.map { entry ->
            entry.key.toBackendEnum<SrvaEventType>() to entry.value.map { it.toCommonSrvaTypeDetail() }
        }?.toMap() ?: mapOf(),
        possibleEventResults = results.map { it.toBackendEnum() },
        possibleEventResultDetails = resultDetails?.map { entry ->
            entry.key.toBackendEnum<SrvaEventResult>() to entry.value.map { it.toBackendEnum<SrvaEventResultDetail>() }
        }?.toMap() ?: mapOf(),
        possibleMethods = methods.map { it.method.toBackendEnum() }
    )
}

