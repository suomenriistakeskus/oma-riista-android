package fi.riista.common.domain.huntingclub.clubs.storage

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingclub.DbOrganization
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.model.LocalizedString
import fi.riista.common.util.executeAsSet
import kotlinx.coroutines.withContext

internal class HuntingClubRepository(
    database: RiistaDatabase
): HuntingClubStorage {
    internal val clubQueries = database.dbOrganizationQueries

    override fun findByRemoteId(organizationId: OrganizationId): Organization? {
        return clubQueries.selectOrganizationByRemoteId(organization_remote_id = organizationId)
            .executeAsOneOrNull()?.toOrganization()
    }

    override fun findByOfficialCode(officialCode: String): Organization? {
        return clubQueries.selectOrganizationByOfficialCode(organization_official_code = officialCode)
            .executeAsOneOrNull()?.toOrganization()
    }

    override suspend fun addOrganizationsIfNotExists(
        organizations: List<Organization>
    ) = withContext(DatabaseWriteContext) {
        clubQueries.transaction {
            val existingOrganizationIds = clubQueries.organizationIds().executeAsSet()
            organizations
                .filter { !existingOrganizationIds.contains(it.id) }
                .forEach { organization ->
                    clubQueries.insertOrganization(organization.toDbOrganization())
                }
        }
    }
}

internal fun Organization.toDbOrganization() = DbOrganization(
    organization_remote_id = id,
    organization_official_code = officialCode,
    organization_name_fi = name.fi,
    organization_name_sv = name.sv,
    organization_name_en = name.en,
)

internal fun DbOrganization.toOrganization() = Organization(
    id = organization_remote_id,
    name = LocalizedString(
        fi = organization_name_fi,
        sv = organization_name_sv,
        en = organization_name_en,
    ),
    officialCode = organization_official_code,
)
