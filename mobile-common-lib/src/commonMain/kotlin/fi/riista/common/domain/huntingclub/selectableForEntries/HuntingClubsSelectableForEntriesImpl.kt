package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubStorage
import fi.riista.common.domain.huntingclub.dto.toOrganization
import fi.riista.common.domain.huntingclub.memberships.HuntingClubOccupationsProvider
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.LocalizedStringComparator

internal class HuntingClubsSelectableForEntriesImpl(
    private val usernameProvider: UsernameProvider,

    /**
     * All clubs
     */
    private val clubStorage: HuntingClubStorage,

    /**
     * User occupations (i.e. memberships)
     */
    private val clubOccupationsProvider: HuntingClubOccupationsProvider,

    /**
     * Clubs that have been previously selected for entries
     */
    private val selectedClubsForEntriesStorage: UserHuntingClubsProvider,

    private val backendApiProvider: BackendApiProvider,
): HuntingClubsSelectableForEntries {
    private val searchResults = UserHuntingClubsMemoryStorage()

    override fun findSelectableClub(organizationId: OrganizationId): Organization? {
        return clubStorage.findByRemoteId(organizationId = organizationId)
    }

    override suspend fun searchClubByOfficialCode(officialCode: String): Organization? {
        val username = usernameProvider.username ?: kotlin.run {
            logger.w { "Refusing to search hunting club. No username" }
            return null
        }

        // try to find from previously search results / club storage
        val existingOrganization = searchResults.findClub(username, officialCode)
            ?: clubStorage.findByOfficialCode(officialCode)?.also {
                // remember to add to search results so that it is available as selectable club!
                searchResults.addClub(username, it)
            }
        if (existingOrganization != null) {
            logger.v { "Club with officialCode=$officialCode already locally cached. No need to search." }
            return existingOrganization
        }

        val response = backendApiProvider.backendAPI.searchHuntingClubByOfficialCode(officialCode)

        val organization = response.transformSuccessData { _, data ->
            data.typed.toOrganization()
        }

        if (organization != null) {
            logger.d { "Found organization (id = ${organization.id}) with officialCode = $officialCode" }

            // club should not exist i.e. it should be safe to add to search results.
            // -> only store to search results. DON'T STORE TO PERMANENT STORAGE YET.
            searchResults.addClub(username = username, club = organization)
        } else {
            logger.d { "Failed to find a hunting club with officialCode=$officialCode" }
        }

        return organization
    }

    override fun getClubsSelectableForEntries(): List<Organization> {
        val username = usernameProvider.username ?: kotlin.run {
            logger.w { "Refusing to persist hunting club. No username" }
            return emptyList()
        }

        val results = clubOccupationsProvider.getOccupations(username).map { it.organisation } +
                selectedClubsForEntriesStorage.getClubs(username) +
                searchResults.getClubs(username).toList().sortedWith { a, b ->
                    LocalizedStringComparator.compare(a.name, b.name).takeIf { it != 0 }
                        ?: a.id.compareTo(b.id)
                }

        return results.distinct()
    }

    override fun getNonPersistentClubs(): List<Organization> {
        val username = usernameProvider.username ?: kotlin.run {
            logger.d { "No username, returning empty non-persistent clubs" }
            return emptyList()
        }

        // non persistent aka search results
        return searchResults.getClubs(username).toList()
    }

    override fun restoreNonPersistentClubs(clubs: List<Organization>) {
        val username = usernameProvider.username ?: kotlin.run {
            logger.d { "No username, not restoring ${clubs.size} non-persistent clubs" }
            return
        }

        clubs.forEach {
            searchResults.addClub(username, it)
        }
    }

    companion object {
        private val logger by getLogger(HuntingClubsSelectableForEntries::class)
    }
}
