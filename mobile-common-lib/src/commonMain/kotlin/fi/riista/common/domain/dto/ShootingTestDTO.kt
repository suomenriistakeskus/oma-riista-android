package fi.riista.common.domain.dto

import fi.riista.common.domain.model.ShootingTest
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.logging.getLogger
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestDTO(
    val rhyCode: String? = null,
    val rhyName: String? = null,
    val type: ShootingTestTypeDTO,
    val typeName: String? = null,

    val begin: LocalDateDTO,
    val end: LocalDateDTO,
    val expired: Boolean,
)

fun ShootingTestDTO.toShootingTest(): ShootingTest? {
    val beginDate = begin.toLocalDate() ?: kotlin.run {
        logger.w { "Failed to parse begin date of shooting test $this" }
        return null
    }
    val endDate = end.toLocalDate() ?: kotlin.run {
        logger.w { "Failed to parse end date of shooting test $this" }
        return null
    }
    return ShootingTest(
        rhyCode = rhyCode,
        rhyName = rhyName,
        type = type.toBackendEnum(),
        typeName = typeName,
        begin = beginDate,
        end = endDate,
        expired = expired,
    )
}

private val logger by getLogger("ShootingTestDTO")
