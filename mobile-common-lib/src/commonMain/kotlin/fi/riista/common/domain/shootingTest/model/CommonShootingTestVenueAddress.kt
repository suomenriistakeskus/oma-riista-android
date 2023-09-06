package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.Revision

data class CommonShootingTestVenueAddress(
    val id: Long,
    val rev: Revision,
    val streetAddress: String?,
    val postalCode: String?,
    val city: String?,
)
