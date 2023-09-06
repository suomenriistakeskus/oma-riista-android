package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.Revision

typealias ShootingTestParticipantId = Long

data class CommonShootingTestParticipant(
    val id: ShootingTestParticipantId,
    val rev: Revision,
    val firstName: String?,
    val lastName: String?,
    val hunterNumber: String?,
    val mooseTestIntended: Boolean,
    val bearTestIntended: Boolean,
    val deerTestIntended: Boolean,
    val bowTestIntended: Boolean,
    val attempts: List<CommonShootingTestParticipantAttempt>,
    val totalDueAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val registrationTime: String?,
    val completed: Boolean,
)
