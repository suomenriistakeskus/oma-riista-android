package fi.riista.common.domain.shootingTest.model

import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.model.BackendEnum

data class CommonShootingTestParticipantAttempt(
    val type: BackendEnum<ShootingTestType>,
    val attemptCount: Int,
    val qualified: Boolean,
)
