package fi.riista.common.domain.huntingclub.clubs.storage

import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId

class MockHuntingClubMemoryStorage: HuntingClubStorage {
    private val organizationsByOfficialCode = mutableMapOf<String, Organization>()

    override fun findByRemoteId(organizationId: OrganizationId): Organization? {
        return organizationsByOfficialCode.values.firstOrNull { it.id == organizationId }
    }

    override fun findByOfficialCode(officialCode: String): Organization? {
        return organizationsByOfficialCode[officialCode]
    }

    override suspend fun addOrganizationsIfNotExists(organizations: List<Organization>) {
        organizations.forEach {
            organizationsByOfficialCode[it.officialCode] = it
        }
    }
}
