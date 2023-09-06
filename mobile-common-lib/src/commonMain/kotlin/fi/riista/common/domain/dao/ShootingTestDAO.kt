package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalDateDAO
import fi.riista.common.domain.model.ShootingTest
import fi.riista.common.dto.toLocalDate
import fi.riista.common.logging.getLogger
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class ShootingTestDAO(
    val rhyCode: String?,
    val rhyName: String?,
    val type: ShootingTestTypeDAO,
    val typeName: String?,

    val begin: LocalDateDAO,
    val end: LocalDateDAO,
    val expired: Boolean,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun ShootingTest.toShootingTestDAO(): ShootingTestDAO? {
    val typeValue = type.rawBackendEnumValue ?: return null

    return ShootingTestDAO(
        rhyCode = rhyCode,
        rhyName = rhyName,
        type = typeValue,
        typeName = typeName,
        begin = begin.toStringISO8601(),
        end = end.toStringISO8601(),
        expired = expired,
    )
}

internal fun ShootingTestDAO.toShootingTest(): ShootingTest? {
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

private val logger by getLogger("ShootingTestDAO")
