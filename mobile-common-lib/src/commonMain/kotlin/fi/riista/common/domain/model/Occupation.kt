package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalizedString

typealias OccupationId = Long

data class Occupation(
    val id: OccupationId,
    val occupationType: BackendEnum<OccupationType>,
    val name: LocalizedString,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val organisation: Organization,
)

