package fi.riista.common.domain.srva.metadata.dto

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.model.toBackendEnum
import fi.riista.common.domain.srva.model.CommonSrvaTypeDetail
import kotlinx.serialization.Serializable

/**
 * A DTO for srva type detail.
 */
@Serializable
data class SrvaTypeDetailDTO(
    val detailType: String,
    // null or empty if applicable for all species, otherwise to be used for all species
    val speciesCodes: List<SpeciesCode>? = null,
)

internal fun SrvaTypeDetailDTO.toCommonSrvaTypeDetail() : CommonSrvaTypeDetail {
    return CommonSrvaTypeDetail(
        detailType = detailType.toBackendEnum(),
        speciesCodes = speciesCodes ?: listOf()
    )
}