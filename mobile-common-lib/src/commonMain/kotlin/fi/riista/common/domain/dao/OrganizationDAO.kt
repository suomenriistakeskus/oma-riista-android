package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.domain.model.Organization
import kotlinx.serialization.Serializable

/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class OrganizationDAO(
    val id: Long,
    val name: LocalizedStringDAO,
    val officialCode: String,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun Organization.toOrganizationDAO(): OrganizationDAO {
    return OrganizationDAO(
        id = id,
        name = name.toLocalizedStringDAO(),
        officialCode = officialCode
    )
}

internal fun OrganizationDAO.toOrganization(): Organization {
    return Organization(
        id = id,
        name = name.toLocalizedString(),
        officialCode = officialCode
    )
}