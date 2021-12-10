package fi.riista.common.dto

import fi.riista.common.model.PersonContactInfo
import kotlinx.serialization.Serializable

@Serializable
data class PersonContactInfoDTO(
    val id: Long? = null,
    val rev: Int? = null,
    val firstName: String? = null,
    val byName: String? = null,
    val lastName: String,
    val hunterNumber: HunterNumberDTO? = null,
    val address: AddressDTO? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val registered: Boolean? = null,
    val adult: Boolean? = null,
)

fun PersonContactInfoDTO.toPersonContactInfo(): PersonContactInfo {
    return PersonContactInfo(
        id = id,
        rev = rev,
        firstName = firstName,
        byName = byName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        address = address?.toAddress(),
        email = email,
        phoneNumber = phoneNumber,
        registered = registered,
        adult = adult,
    )
}
