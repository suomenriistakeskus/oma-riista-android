package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.srva.MockSrvaEventData
import fi.riista.common.domain.srva.MockSrvaEventPageData
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.toCommonSrvaEvent
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

class DeletedSrvaEventsUpdaterTest {

    @Test
    fun testSendDeletedEventToBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendAPI = BackendAPIMock()
        val updater = getDeletedSrvaEventsUpdater(backendAPI = backendAPI, database = database)

        // Add two events to database and mark one of them as deleted
        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)
        val insertedEvent1 = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event.copy(mobileClientRefId = null),
        )
        val insertedEvent2 = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event.copy(remoteId = (event.remoteId ?: 0) + 1, mobileClientRefId = null),
        )
        assertNotNull(repository.markDeleted(srvaEventLocalId = insertedEvent1.localId))

        // Check that correct event was deleted from backend
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteSrvaEvent.name))
        val deletedEventRemoteId = backendAPI.callParameter(BackendAPI::deleteSrvaEvent.name) as Long
        assertEquals(insertedEvent1.remoteId, deletedEventRemoteId)

        // Check that database has only second inserted event left
        val dbEvents = repository.listEvents(username)
        assertEquals(1, dbEvents.size)
        assertEquals(insertedEvent2.remoteId, dbEvents[0].remoteId)

        // Check that deleted event is now deleted also from database
        val deletedEvents = repository.getDeletedEvents(username)
        assertEquals(0, deletedEvents.size)
    }

    @Test
    fun testDeletingEventFromBackendFails() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendAPI = BackendAPIMock(deleteSrvaEventResponse = MockResponse.error(500))
        val updater = getDeletedSrvaEventsUpdater(backendAPI = backendAPI, database = database)

        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)
        val insertedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event,
        )
        assertNotNull(repository.markDeleted(srvaEventLocalId = insertedEvent.localId))
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteSrvaEvent.name))
        val deletedEventRemoteId = backendAPI.callParameter(BackendAPI::deleteSrvaEvent.name) as Long
        assertEquals(insertedEvent.remoteId, deletedEventRemoteId)

        // Event should still be in database
        val deletedEvents = repository.getDeletedEvents(username)
        assertEquals(1, deletedEvents.size)
        assertEquals(insertedEvent.remoteId, deletedEvents[0].remoteId)
    }

    @Test
    fun testGetDeletedEventsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendAPI = BackendAPIMock(deleteSrvaEventResponse = MockResponse.error(500))
        val updater = getDeletedSrvaEventsUpdater(backendAPI = backendAPI, database = database)

        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)
        repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event,
        )
        assertEquals(1, repository.listEvents(username).size)

        val deletedAfter = event.pointOfTime.changeTime(hour = (event.pointOfTime.hour - 1))
        updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)

        assertEquals(0, repository.listEvents(username).size)
    }

    @Test
    fun testEmptyDeletedEventsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val backendAPI = BackendAPIMock(deleteSrvaEventResponse = MockResponse.error(500))
        val updater = getDeletedSrvaEventsUpdater(backendAPI = backendAPI, database = database)

        val deletedAfter = LocalDateTime(2020, 10, 3, 12, 0, 0)
        backendAPI.fetchDeletedSrvaEventsResponse = MockResponse.success(MockSrvaEventPageData.emptyDeletedSrvaEvents)
        val timestamp = updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)
        assertNull(timestamp)
    }

    private fun getDeletedSrvaEventsUpdater(
        backendAPI: BackendAPI = BackendAPIMock(),
        database: RiistaDatabase,
    ): DeletedSrvaEventsUpdater {
        return DeletedSrvaEventsUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            database = database,
        )
    }
}
