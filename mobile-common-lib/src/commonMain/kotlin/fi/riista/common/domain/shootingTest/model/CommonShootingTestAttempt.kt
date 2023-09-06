package fi.riista.common.domain.shootingTest.model

import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.Revision

typealias ShootingTestAttemptId = Long

data class CommonShootingTestAttempt(
    val id: ShootingTestAttemptId,
    val rev: Revision,
    val participantId: ShootingTestParticipantId?,
    val participantRev: Revision?,
    val type: BackendEnum<ShootingTestType>,
    val result: BackendEnum<ShootingTestResult>,
    val hits: Int,
    val note: String? = null,
)
