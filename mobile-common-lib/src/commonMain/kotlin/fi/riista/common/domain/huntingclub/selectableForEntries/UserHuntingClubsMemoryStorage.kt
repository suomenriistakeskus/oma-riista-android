package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.model.Organization

internal class UserHuntingClubsMemoryStorage: UserHuntingClubsProvider {
    private val clubs = mutableMapOf<String, MutableList<Organization>>()

    override fun findClub(username: String, clubOfficialCode: String): Organization? {
        return getClubs(username).firstOrNull { it.officialCode == clubOfficialCode }
    }

    override fun getClubs(username: String): List<Organization> {
        return clubs[username] ?: listOf()
    }

    fun addClub(username: String, club: Organization) {
        val existingClub = findClub(
            username = username,
            clubOfficialCode = club.officialCode
        )

        if (existingClub == null) {
            // only allow adding if not already added
            clubs.getOrPut(
                key = username,
                defaultValue = { mutableListOf() }
            ).add(club)
        }

    }
}

