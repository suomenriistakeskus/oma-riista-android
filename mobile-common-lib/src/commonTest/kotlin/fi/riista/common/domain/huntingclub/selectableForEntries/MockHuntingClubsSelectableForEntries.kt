package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId

class MockHuntingClubsSelectableForEntries(
    var selectableClubs: MutableList<Organization> = mutableListOf(),
    var searchableClubs: MutableList<Organization> = mutableListOf(),
): HuntingClubsSelectableForEntries {
    override fun getClubsSelectableForEntries(): List<Organization> {
        return selectableClubs
    }

    override fun findSelectableClub(organizationId: OrganizationId): Organization? {
        return selectableClubs.firstOrNull { it.id == organizationId }
    }

    override suspend fun searchClubByOfficialCode(officialCode: String): Organization? {
        return searchableClubs.firstOrNull { it.officialCode == officialCode }
    }

    override fun getNonPersistentClubs(): List<Organization> {
        return emptyList()
    }

    override fun restoreNonPersistentClubs(clubs: List<Organization>) {
        // nop
    }
}
