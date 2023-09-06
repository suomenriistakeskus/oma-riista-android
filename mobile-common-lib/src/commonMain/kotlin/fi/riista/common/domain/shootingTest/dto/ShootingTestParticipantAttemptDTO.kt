package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipantAttempt
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestParticipantAttemptDTO(
    val type: String,
    val attemptCount: Int,
    val qualified: Boolean,
)

fun ShootingTestParticipantAttemptDTO.toCommonShootingTestParticipantAttempt() =
    CommonShootingTestParticipantAttempt(
        type = type.toBackendEnum(),
        attemptCount = attemptCount,
        qualified = qualified
    )
