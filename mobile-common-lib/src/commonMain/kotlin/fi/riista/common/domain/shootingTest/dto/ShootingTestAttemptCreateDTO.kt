package fi.riista.common.domain.shootingTest.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestAttemptCreateDTO(
    val participantId: Long,
    val participantRev: Int,
    val type: String,
    val result: String,
    val hits: Int,
    val note: String?,
)
