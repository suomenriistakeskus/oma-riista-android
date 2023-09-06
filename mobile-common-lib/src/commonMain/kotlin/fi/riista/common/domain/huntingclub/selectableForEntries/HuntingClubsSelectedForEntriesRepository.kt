package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubStorage
import fi.riista.common.domain.huntingclub.clubs.storage.toOrganization
import fi.riista.common.domain.model.Organization

/**
 * A repository for accessing the hunting clubs that have been selected in diary entries (e.g. harvests)
 */
internal class HuntingClubsSelectedForEntriesRepository(
    database: RiistaDatabase,
    private val clubStorage: HuntingClubStorage,
): UserHuntingClubsProvider {
    private val dbClubsSelectedForEntriesQueries = database.dbClubsSelectedForEntriesQueries

    override fun findClub(username: String, clubOfficialCode: String): Organization? {
        return clubStorage.findByOfficialCode(clubOfficialCode)
    }

    override fun getClubs(username: String): List<Organization> {
        return dbClubsSelectedForEntriesQueries.listClubsSelectedForHarvest(username = username)
            .executeAsList()
            .map { it.toOrganization() }
    }
}
