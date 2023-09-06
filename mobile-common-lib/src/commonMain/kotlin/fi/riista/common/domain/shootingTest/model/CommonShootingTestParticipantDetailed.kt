package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.LocalDate
import fi.riista.common.model.Revision

data class CommonShootingTestParticipantDetailed(
    val id: ShootingTestParticipantId,
    val rev: Revision,
    val firstName: String?,
    val lastName: String?,
    val hunterNumber: String?,
    val dateOfBirth: LocalDate?,
    val mooseTestIntended: Boolean,
    val bearTestIntended: Boolean,
    val deerTestIntended: Boolean,
    val bowTestIntended: Boolean,
    val registrationTime: String?,
    val completed: Boolean,
    val attempts: List<CommonShootingTestAttempt>,
)
