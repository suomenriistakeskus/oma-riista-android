package fi.riista.common.domain.model

typealias AddressId = Long

data class Address(
    val id: AddressId,
    val rev: Int,
    val editable: Boolean,
    val streetAddress: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val country: String? = null,
)
