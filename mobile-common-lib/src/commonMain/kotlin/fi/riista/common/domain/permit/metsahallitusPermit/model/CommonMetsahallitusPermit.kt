package fi.riista.common.domain.permit.metsahallitusPermit.model

import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalizedString

/**
 * A model representing single Metsahallitus permit.
 */
data class CommonMetsahallitusPermit(
    val permitIdentifier: String,
    val permitType: LocalizedString,
    val permitName: LocalizedString,

    val areaNumber: String,
    val areaName: LocalizedString,

    val beginDate: LocalDate?,
    val endDate: LocalDate?,

    val harvestFeedbackUrl: LocalizedString?,
)
