package fi.riista.common.model

typealias OccupationId = Long

data class Occupation(
    val id: OccupationId,
    val occupationType: BackendEnum<OccupationType>,
    val name: LocalizedString,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val organisation: Organization,
)

