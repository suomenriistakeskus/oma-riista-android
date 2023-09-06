package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.Revision

data class CommonShootingTestVenue(
    val id: Long?,
    val rev: Revision,
    val name: String?,
    val address: CommonShootingTestVenueAddress?,
    val info: String?,
)
