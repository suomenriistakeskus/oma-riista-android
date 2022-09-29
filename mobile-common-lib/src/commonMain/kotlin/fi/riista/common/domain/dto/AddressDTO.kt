package fi.riista.common.domain.dto

import fi.riista.common.domain.model.Address
import kotlinx.serialization.Serializable

@Serializable
data class AddressDTO(
    val id: Long,
    val rev: Int,
    val editable: Boolean,
    val streetAddress: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val country: String? = null,
)

fun AddressDTO.toAddress(): Address {
    return Address(
        id = id,
        rev = rev,
        editable = editable,
        streetAddress = streetAddress,
        postalCode = postalCode,
        city = city,
        country = country,
    )
}
