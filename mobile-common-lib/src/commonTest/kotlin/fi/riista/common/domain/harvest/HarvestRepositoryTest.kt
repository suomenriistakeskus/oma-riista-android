package fi.riista.common.domain.harvest

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.ui.HarvestTestData
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.helpers.createDatabaseDriverFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class HarvestRepositoryTest {

    private val username = "user"

    @Test
    fun getAndUpdateShooters() {
        val repository = getRepository()

        val harvest = HarvestTestData.createHarvest().copy(id = 1, actorInfo = GroupHuntingPerson.Guest(person1))
        runBlocking {
            repository.upsertHarvest(
                username = username,
                harvest = harvest,
            )
            repository.upsertHarvest(
                username = username,
                harvest = HarvestTestData.createHarvest().copy(id = 2, actorInfo = GroupHuntingPerson.Guest(person2)),
            )
        }
        var shooters = repository.getShooters(username)
        assertEquals(2, shooters.size)
        assertEquals(setOf(person1, person2), shooters.toSet())

        runBlocking {
            repository.upsertHarvest(
                username = username,
                harvest = harvest.copy(actorInfo = GroupHuntingPerson.Guest(person1Updated)),
            )
        }
        shooters = repository.getShooters(username)
        assertEquals(2, shooters.size)
        assertEquals(setOf(person1Updated, person2), shooters.toSet())
    }

    private fun getRepository(): HarvestRepository {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        return HarvestRepository(database)
    }

    private val person1 = PersonWithHunterNumber(
        id = 666,
        rev = 1,
        byName = "Matti",
        lastName = "Meikäläinen",
        hunterNumber = "22222222",
    )
    private val person1Updated = person1.copy(rev = 2, byName = "Mattias")
    private val person2 = PersonWithHunterNumber(
        id = 777,
        rev = 1,
        byName = "Maija",
        lastName = "Mattila",
        hunterNumber = "33333333",
    )
}
