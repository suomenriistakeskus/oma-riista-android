package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipant
import fi.riista.common.dto.DateTimeDTO
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestParticipantDTO(
    val id: Long,
    val rev: Int,
    val firstName: String? = null,
    val lastName: String? = null,
    val hunterNumber: String? = null,
    val mooseTestIntended: Boolean,
    val bearTestIntended: Boolean,
    val deerTestIntended: Boolean,
    val bowTestIntended: Boolean,
    val attempts: List<ShootingTestParticipantAttemptDTO> = listOf(),
    val totalDueAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val registrationTime: DateTimeDTO? = null,
    val completed: Boolean,
)

fun ShootingTestParticipantDTO.toCommonShootingTestParticipant() =
    CommonShootingTestParticipant(
        id = id,
        rev = rev,
        firstName = firstName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        mooseTestIntended = mooseTestIntended,
        bearTestIntended = bearTestIntended,
        deerTestIntended = deerTestIntended,
        bowTestIntended = bowTestIntended,
        attempts = attempts.map { it.toCommonShootingTestParticipantAttempt() },
        totalDueAmount = totalDueAmount,
        paidAmount = paidAmount,
        remainingAmount = remainingAmount,
        registrationTime = registrationTime,
        completed = completed,
    )
