package fi.riista.common.domain.permit.metsahallitusPermit.dto

import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalizedString
import kotlinx.serialization.Serializable

/**
 * A model representing single Metsahallitus permit.
 */
@Serializable
internal data class CommonMetsahallitusPermitDTO(
    val permitIdentifier: String,
    val permitType: LocalizedStringDTO,
    val permitName: LocalizedStringDTO,

    val areaNumber: String,
    val areaName: LocalizedStringDTO,

    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,

    val harvestFeedbackUrl: LocalizedStringDTO? = null,
)

internal fun CommonMetsahallitusPermitDTO.toCommonMetsahallitusPermit() =
    CommonMetsahallitusPermit(
        permitIdentifier = permitIdentifier,
        permitType = permitType.toLocalizedString(),
        permitName = permitName.toLocalizedString(),
        areaNumber = areaNumber,
        areaName = areaName.toLocalizedString(),
        beginDate = beginDate?.toLocalDate(),
        endDate = endDate?.toLocalDate(),
        harvestFeedbackUrl = harvestFeedbackUrl?.toLocalizedString(),
    )
