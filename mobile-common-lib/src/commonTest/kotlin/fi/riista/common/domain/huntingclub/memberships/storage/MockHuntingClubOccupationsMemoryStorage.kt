package fi.riista.common.domain.huntingclub.memberships.storage

import fi.riista.common.domain.model.Occupation
import fi.riista.common.domain.model.OccupationId

class MockHuntingClubOccupationsMemoryStorage: HuntingClubOccupationsStorage {
    private val occupations = mutableMapOf<String, List<Occupation>>()

    fun hasOccupations(username: String) = getOccupations(username).isNotEmpty()

    fun getOccupation(username: String, id: OccupationId): Occupation? {
        return getOccupations(username).firstOrNull { it.id == id }
    }

    override fun getOccupations(username: String): List<Occupation> {
        return occupations[username] ?: listOf()
    }

    override suspend fun replaceOccupations(username: String, occupations: List<Occupation>) {
        this.occupations[username] = occupations
    }
}
