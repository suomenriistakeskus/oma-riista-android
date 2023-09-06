package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestVenueAddress
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestVenueAddressDTO(
    val id: Long,
    val rev: Int,
    val streetAddress: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
)

fun ShootingTestVenueAddressDTO.toShootingTestVenueAddress() =
    CommonShootingTestVenueAddress(
        id = id,
        rev = rev,
        streetAddress = streetAddress,
        postalCode = postalCode,
        city = city,
    )
