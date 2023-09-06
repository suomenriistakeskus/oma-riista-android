package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestAttempt
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestAttemptDTO(
    val id: Long,
    val rev: Int,
    val participantId: Long? = null,
    val participantRev: Int? = null,
    val type: String,
    val result: String? = null,
    val hits: Int,
    val note: String? = null,
)

fun ShootingTestAttemptDTO.toCommonShootingTestAttempt() =
    CommonShootingTestAttempt(
        id = id,
        rev = rev,
        participantId = participantId,
        participantRev = participantRev,
        type = type.toBackendEnum(),
        result = result.toBackendEnum(),
        hits = hits,
        note = note,
    )
