package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestOfficial
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestOfficialDTO(
    val id: Long? = null,
    val shootingTestEventId: Long? = null,
    val occupationId: Long,
    val personId: Long,
    val firstName: String? = null,
    val lastName: String? = null,
    val shootingTestResponsible: Boolean? = null,
)

fun ShootingTestOfficialDTO.toCommonShootingTestOfficial() =
    CommonShootingTestOfficial(
        id = id,
        shootingTestEventId = shootingTestEventId,
        occupationId = occupationId,
        personId = personId,
        firstName = firstName,
        lastName = lastName,
        shootingTestResponsible = shootingTestResponsible ?: false,
    )
