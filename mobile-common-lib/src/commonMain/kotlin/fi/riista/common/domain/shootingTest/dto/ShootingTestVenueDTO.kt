package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestVenue
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestVenueDTO(
    val id: Long? = null,
    val rev: Int,
    val name: String? = null,
    val address: ShootingTestVenueAddressDTO? = null,
    val info: String? = null,
)

fun ShootingTestVenueDTO.toShootingTestVenue() =
    CommonShootingTestVenue(
        id = id,
        rev = rev,
        name = name,
        address = address?.toShootingTestVenueAddress(),
        info = info,
    )
