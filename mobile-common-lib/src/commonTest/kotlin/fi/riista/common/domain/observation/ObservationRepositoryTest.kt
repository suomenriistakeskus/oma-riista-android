package fi.riista.common.domain.observation

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.toCommonObservation
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ObservationRepositoryTest {
    @Test
    fun testObservationWithNoSpecimens() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)

        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)

        val insertedObservation = repository.upsertObservation(
            username = username,
            observation = observation,
        )
        assertNull(insertedObservation.specimens)
        assertNull(insertedObservation.totalSpecimenAmount)

        val repositoryObservations = repository.listObservations(username)
        assertEquals(1, repositoryObservations.size)
        val repositoryObservation = repositoryObservations.first()
        assertNull(repositoryObservation.specimens)
        assertNull(repositoryObservation.totalSpecimenAmount)
    }
}
