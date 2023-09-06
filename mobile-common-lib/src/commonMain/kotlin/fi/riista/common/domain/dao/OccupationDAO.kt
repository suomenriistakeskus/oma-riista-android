package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalDateDAO
import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.domain.model.Occupation
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class OccupationDAO(
    val id: Long,
    val occupationType: OccupationTypeDAO,
    val name: LocalizedStringDAO,

    val beginDate: LocalDateDAO?,
    val endDate: LocalDateDAO?,
    val organisation: OrganizationDAO
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun Occupation.toOccupationDAO(): OccupationDAO? {
    val occupationTypeValue = this.occupationType.rawBackendEnumValue ?: return null

    return OccupationDAO(
        id = id,
        occupationType = occupationTypeValue,
        name = name.toLocalizedStringDAO(),
        beginDate = beginDate?.toStringISO8601(),
        endDate = endDate?.toStringISO8601(),
        organisation = organisation.toOrganizationDAO(),
    )
}

internal fun OccupationDAO.toOccupation(): Occupation {
    return Occupation(
        id = id,
        occupationType = occupationType.toBackendEnum(),
        name = name.toLocalizedString(),
        beginDate = beginDate?.toLocalDate(),
        endDate = endDate?.toLocalDate(),
        organisation = organisation.toOrganization(),
    )
}
