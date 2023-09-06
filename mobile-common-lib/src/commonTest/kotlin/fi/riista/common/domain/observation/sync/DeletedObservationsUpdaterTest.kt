package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.observation.MockObservationData
import fi.riista.common.domain.observation.MockObservationPageData
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.toCommonObservation
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.changeTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeletedObservationsUpdaterTest {

    @Test
    fun testSendDeletedObservationToBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendAPI = BackendAPIMock()
        val updater = getDeletedObservationsUpdater(backendAPI = backendAPI, database = database)

        // Add two observations to database and mark one of them as deleted
        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)
        val insertedObservation1 = repository.upsertObservation(
            username = username,
            observation = observation.copy(mobileClientRefId = null),
        )
        val insertedObservation2 = repository.upsertObservation(
            username = username,
            observation = observation.copy(remoteId = (observation.remoteId ?: 0) + 1, mobileClientRefId = null),
        )
        assertNotNull(repository.markDeleted(observationLocalId = insertedObservation1.localId))

        // Check that correct observation was deleted from backend
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteObservation.name))
        val deletedObservationRemoteId = backendAPI.callParameter(BackendAPI::deleteObservation.name) as Long
        assertEquals(insertedObservation1.remoteId, deletedObservationRemoteId)

        // Check that database has only second inserted observation left
        val dbObservations = repository.listObservations(username)
        assertEquals(1, dbObservations.size)
        assertEquals(insertedObservation2.remoteId, dbObservations[0].remoteId)

        // Check that deleted observation is now deleted also from database
        val deletedEvents = repository.getDeletedObservations(username)
        assertEquals(0, deletedEvents.size)
    }

    @Test
    fun testDeletingObservationFromBackendFails() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendAPI = BackendAPIMock(deleteObservationResponse = MockResponse.error(500))
        val updater = getDeletedObservationsUpdater(backendAPI = backendAPI, database = database)

        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)
        val insertedObservation = repository.upsertObservation(
            username = username,
            observation = observation,
        )
        assertNotNull(repository.markDeleted(observationLocalId = insertedObservation.localId))
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteObservation.name))
        val deletedEventRemoteId = backendAPI.callParameter(BackendAPI::deleteObservation.name) as Long
        assertEquals(insertedObservation.remoteId, deletedEventRemoteId)

        // Event should still be in database
        val deletedObservations = repository.getDeletedObservations(username)
        assertEquals(1, deletedObservations.size)
        assertEquals(insertedObservation.remoteId, deletedObservations[0].remoteId)
    }

    @Test
    fun testGetDeletedObservationsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendAPI = BackendAPIMock()
        val updater = getDeletedObservationsUpdater(backendAPI = backendAPI, database = database)

        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)
        repository.upsertObservation(
            username = username,
            observation = observation,
        )
        assertEquals(1, repository.listObservations(username).size)

        val deletedAfter = observation.pointOfTime.changeTime(hour = (observation.pointOfTime.hour - 1))
        updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)

        assertEquals(0, repository.listObservations(username).size)
    }

    @Test
    fun testEmptyDeletedObservationsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val backendAPI = BackendAPIMock(
            fetchDeletedObservationsResponse = MockResponse.success(MockObservationPageData.emptyDeletedObservations)
        )
        val updater = getDeletedObservationsUpdater(backendAPI = backendAPI, database = database)

        val deletedAfter = LocalDateTime(2020, 10, 3, 12, 0, 0)
        val timestamp = updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)
        assertNull(timestamp)
    }

    private fun getDeletedObservationsUpdater(
        backendAPI: BackendAPI = BackendAPIMock(),
        database: RiistaDatabase,
    ): DeletedObservationsUpdater {
        return DeletedObservationsUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            database = database,
        )
    }
}
