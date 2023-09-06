package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.MockHarvestData
import fi.riista.common.domain.harvest.MockHarvestPageData
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.toCommonHarvest
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

class DeletedHarvestsUpdaterTest {

    @Test
    fun testSendDeletedHarvestToBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendAPI = BackendAPIMock()
        val updater = getDeletedHarvestsUpdater(backendAPI = backendAPI, database = database)

        // Add two harvests to database and mark one of them as deleted
        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)
        val insertedHarvest1 = repository.upsertHarvest(
            username = username,
            harvest = harvest.copy(mobileClientRefId = null),
        )
        val insertedHarvest2 = repository.upsertHarvest(
            username = username,
            harvest = harvest.copy(id = (harvest.id ?: 0) + 1, mobileClientRefId = null),
        )
        assertNotNull(repository.markDeleted(harvestLocalId = insertedHarvest1.localId))

        // Check that correct harvest was deleted from backend
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteHarvest.name))
        val deletedHarvestRemoteId = backendAPI.callParameter(BackendAPI::deleteHarvest.name) as Long
        assertEquals(insertedHarvest1.id, deletedHarvestRemoteId)

        // Check that database has only second inserted harvest left
        val dbHarvests = repository.listHarvests(username)
        assertEquals(1, dbHarvests.size)
        assertEquals(insertedHarvest2.id, dbHarvests[0].id)

        // Check that deleted harvest is now deleted also from database
        val deletedHarvests = repository.getDeletedHarvests(username)
        assertEquals(0, deletedHarvests.size)
    }

    @Test
    fun testDeletingHarvestFromBackendFails() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendAPI = BackendAPIMock(deleteHarvestResponse = MockResponse.error(500))
        val updater = getDeletedHarvestsUpdater(backendAPI = backendAPI, database = database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)
        val insertedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvest,
        )
        assertNotNull(repository.markDeleted(harvestLocalId = insertedHarvest.localId))
        updater.updateToBackend(username)
        assertEquals(1, backendAPI.totalCallCount())
        assertEquals(1, backendAPI.callCount(BackendAPI::deleteHarvest.name))
        val deletedHarvestRemoteId = backendAPI.callParameter(BackendAPI::deleteHarvest.name) as Long
        assertEquals(insertedHarvest.id, deletedHarvestRemoteId)

        // Harvest should still be in database
        val deletedHarvests = repository.getDeletedHarvests(username)
        assertEquals(1, deletedHarvests.size)
        assertEquals(insertedHarvest.id, deletedHarvests[0].id)
    }

    @Test
    fun testGetDeletedHarvestsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendAPI = BackendAPIMock()
        val updater = getDeletedHarvestsUpdater(backendAPI = backendAPI, database = database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)
        repository.upsertHarvest(
            username = username,
            harvest = harvest,
        )
        assertEquals(1, repository.listHarvests(username).size)

        val deletedAfter = harvest.pointOfTime.changeTime(hour = (harvest.pointOfTime.hour - 1))
        updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)

        assertEquals(0, repository.listHarvests(username).size)
    }

    @Test
    fun testEmptyDeletedHarvestsFromBackend() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val backendAPI = BackendAPIMock(
            fetchDeletedHarvestsResponse = MockResponse.success(MockHarvestPageData.emptyDeletedHarvests)
        )
        val updater = getDeletedHarvestsUpdater(backendAPI = backendAPI, database = database)

        val deletedAfter = LocalDateTime(2020, 10, 3, 12, 0, 0)
        val timestamp = updater.fetchFromBackend(username = username, deletedAfter = deletedAfter)
        assertNull(timestamp)
    }

    private fun getDeletedHarvestsUpdater(
        backendAPI: BackendAPI = BackendAPIMock(),
        database: RiistaDatabase,
    ): DeletedHarvestsUpdater {
        return DeletedHarvestsUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            database = database,
        )
    }
}
