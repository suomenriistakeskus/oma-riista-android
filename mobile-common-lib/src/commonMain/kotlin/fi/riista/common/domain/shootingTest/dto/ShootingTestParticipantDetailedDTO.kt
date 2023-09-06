package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipantDetailed
import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.dto.toLocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestParticipantDetailedDTO(
    val id: Long,
    val rev: Int,
    val firstName: String? = null,
    val lastName: String? = null,
    val hunterNumber: String? = null,
    val dateOfBirth: String? = null,
    val mooseTestIntended: Boolean = false,
    val bearTestIntended: Boolean = false,
    val deerTestIntended: Boolean = false,
    val bowTestIntended: Boolean = false,
    val registrationTime: DateTimeDTO? = null,
    val completed: Boolean = false,
    val attempts: List<ShootingTestAttemptDTO> = listOf(),
)

fun ShootingTestParticipantDetailedDTO.toCommonShootingTestParticipantDetailed() =
    CommonShootingTestParticipantDetailed(
        id = id,
        rev = rev,
        firstName = firstName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        dateOfBirth = dateOfBirth?.toLocalDate(),
        mooseTestIntended = mooseTestIntended,
        bearTestIntended = bearTestIntended,
        deerTestIntended = deerTestIntended,
        bowTestIntended = bowTestIntended,
        registrationTime = registrationTime,
        completed = completed,
        attempts = attempts.map { it.toCommonShootingTestAttempt() },
    )
