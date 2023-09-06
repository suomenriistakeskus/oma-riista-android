package fi.riista.common.domain.shootingTest.dto

import kotlinx.serialization.Serializable

@Serializable
class UpdateShootingTestOfficialsDTO(
    val calendarEventId: Long,
    val shootingTestEventId: Long,
    val occupationIds: List<Long>,
    val responsibleOccupationId: Long?,
)
