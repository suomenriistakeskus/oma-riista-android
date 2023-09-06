package fi.riista.common.domain.shootingTest.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenShootingTestEventDTO(
    val calendarEventId: Long,
    val shootingTestEventId: Long?,
    val occupationIds: List<Long>,
    val responsibleOccupationId: Long?,
)
