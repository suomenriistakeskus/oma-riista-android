package fi.riista.common.domain.model

data class PersonContactInfo(
    val id: Long?,
    val rev: Int?,
    val firstName: String? = null,
    val byName: String? = null,
    val lastName: String,
    val hunterNumber: HunterNumber? = null,
    val address: Address? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val registered: Boolean? = null,
    val adult: Boolean? = null,
)
