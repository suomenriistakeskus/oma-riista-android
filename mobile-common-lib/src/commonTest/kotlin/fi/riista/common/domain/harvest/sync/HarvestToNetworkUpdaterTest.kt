package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.MockHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimenId
import fi.riista.common.domain.harvest.model.toHarvestSpecimenDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.toCommonHarvest
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HarvestToNetworkUpdaterTest {

    @Test
    fun testSendNewHarvestToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendApi = BackendAPIMock()
        val updater = harvestToNetworkUpdater(backendApi, database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        repository.upsertHarvest(username, harvest.copy(id = null, modified = true))
        val modifiedEvents = repository.getModifiedHarvests(username)
        assertEquals(1, modifiedEvents.size)

        updater.update(username, modifiedEvents)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::createHarvest.name))

        val createHarvest = backendApi.callParameter(BackendAPI::createHarvest.name) as HarvestCreateDTO
        assertEquals("HARVEST", createHarvest.type)
        assertEquals(7189988460482656588, createHarvest.mobileClientRefId)
        assertEquals(9, createHarvest.harvestSpecVersion)
        assertEquals(47230, createHarvest.gameSpeciesCode)
        assertNull(createHarvest.deerHuntingType)
        assertTrue(createHarvest.canEdit)
        assertEquals(6900707, createHarvest.geoLocation.latitude)
        assertEquals(440517, createHarvest.geoLocation.longitude)
        assertEquals("GPS_DEVICE", createHarvest.geoLocation.source)
        assertEquals(12.302000045776367, createHarvest.geoLocation.accuracy)
        assertEquals(21.4, createHarvest.geoLocation.altitude)
        assertEquals(0.6, createHarvest.geoLocation.altitudeAccuracy)
        assertEquals("2023-01-18T15:47:00", createHarvest.pointOfTime)
        assertEquals("Small animal", createHarvest.description)
        assertEquals(1, createHarvest.imageIds.size)
        assertEquals(1, createHarvest.specimens?.size)
        val expectedSpecimens = setOf(
            createCommonHarvestSpecimen(
                id = 1078,
                rev = 3,
                gender =  "FEMALE".toBackendEnum(),
                age = "YOUNG".toBackendEnum(),
                weight = 3.45,
            ).toHarvestSpecimenDTO()
        )
        assertEquals(expectedSpecimens, createHarvest.specimens?.toSet())
    }


    @Test
    fun testSendUpdatedHarvestToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendApi = BackendAPIMock()
        val updater = harvestToNetworkUpdater(backendApi, database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        repository.upsertHarvest(username, harvest.copy(modified = true))
        val modifiedHarvests = repository.getModifiedHarvests(username)
        assertEquals(1, modifiedHarvests.size)

        updater.update(username, modifiedHarvests)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::updateHarvest.name))
        val updateHarvest = backendApi.callParameter(BackendAPI::updateHarvest.name) as HarvestDTO
        assertNotNull(updateHarvest)

        assertEquals(1940, updateHarvest.id)
        assertEquals(5, updateHarvest.rev)
        assertEquals("HARVEST", updateHarvest.type)
        assertEquals(7189988460482656588, updateHarvest.mobileClientRefId)
        assertEquals(9, updateHarvest.harvestSpecVersion)
        assertEquals(47230, updateHarvest.gameSpeciesCode)
        assertNull(updateHarvest.deerHuntingType)
        assertTrue(updateHarvest.canEdit)
        assertEquals(6900707, updateHarvest.geoLocation.latitude)
        assertEquals(440517, updateHarvest.geoLocation.longitude)
        assertEquals("GPS_DEVICE", updateHarvest.geoLocation.source)
        assertEquals(12.302000045776367, updateHarvest.geoLocation.accuracy)
        assertEquals(21.4, updateHarvest.geoLocation.altitude)
        assertEquals(0.6, updateHarvest.geoLocation.altitudeAccuracy)
        assertEquals("2023-01-18T15:47:00", updateHarvest.pointOfTime)
        assertEquals("Small animal", updateHarvest.description)
        assertEquals(1, updateHarvest.imageIds.size)
        assertEquals(1, updateHarvest.specimens?.size)
        val expectedSpecimens = setOf(
            createCommonHarvestSpecimen(
                id = 1078,
                rev = 3,
                gender =  "FEMALE".toBackendEnum(),
                age = "YOUNG".toBackendEnum(),
                weight = 3.45,
            ).toHarvestSpecimenDTO()
        )
        assertEquals(expectedSpecimens, updateHarvest.specimens?.toSet())
}

    private fun harvestToNetworkUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
    ): HarvestToNetworkUpdater {
        return HarvestToNetworkUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
        )
    }

    private fun createCommonHarvestSpecimen(
        id: CommonHarvestSpecimenId?,
        rev: Int?,
        gender: BackendEnum<Gender>,
        age: BackendEnum<GameAge>,
        weight: Double?,
    ) = CommonHarvestSpecimen(
        id = id,
        rev = rev,
        gender = gender,
        age = age,
        weight = weight,
        antlersLost = null,
        notEdible = null,
        alone = null,
        weightEstimated = null,
        weightMeasured = null,
        fitnessClass = BackendEnum.create(null),
        antlersType = BackendEnum.create(null),
        antlersWidth = null,
        antlerPointsLeft = null,
        antlerPointsRight = null,
        antlersGirth = null,
        antlersLength = null,
        antlersInnerWidth = null,
        antlerShaftWidth = null,
        additionalInfo = null,
    )
}
