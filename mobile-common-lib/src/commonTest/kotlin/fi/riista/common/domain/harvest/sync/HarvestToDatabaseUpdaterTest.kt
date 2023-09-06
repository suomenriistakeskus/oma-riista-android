package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.MockHarvestData
import fi.riista.common.domain.harvest.MockHarvestPageData
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimenId
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestPageDTO
import fi.riista.common.domain.harvest.sync.dto.toCommonHarvest
import fi.riista.common.domain.harvest.sync.dto.toHarvestPage
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.GameAntlersType
import fi.riista.common.domain.model.GameFitnessClass
import fi.riista.common.domain.model.Gender
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HarvestToDatabaseUpdaterTest {

    @Test
    fun testInsertAndUpdateObservation() {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = HarvestToDatabaseUpdater(database)
        val repository = HarvestRepository(database)

        runBlocking {
            assertEquals(0, repository.listHarvests(username).size)
        }

        val harvestPage = MockHarvestPageData.harvestPageWithOneHarvest
            .deserializeFromJson<HarvestPageDTO>()
            ?.toHarvestPage()
        assertNotNull(harvestPage)
        runBlocking {
            updater.update(
                username = username,
                harvests = harvestPage.content,
                overwriteNonModified = false
            )
        }

        val insertedHarvests = runBlocking {
            repository.listHarvests(username)
        }
        assertEquals(1, insertedHarvests.size)

        var harvest = insertedHarvests[0]
        assertEquals(1940, harvest.id)
        assertEquals(5, harvest.rev)
        assertEquals(6900707, harvest.geoLocation.latitude)
        assertEquals(440517, harvest.geoLocation.longitude)
        assertEquals("GPS_DEVICE", harvest.geoLocation.source.rawBackendEnumValue)
        assertEquals(12.302000045776367, harvest.geoLocation.accuracy)
        assertEquals(21.4, harvest.geoLocation.altitude)
        assertEquals(0.6, harvest.geoLocation.altitudeAccuracy)
        assertEquals(LocalDateTime(2023, 1, 18, 15, 47, 0), harvest.pointOfTime)
        assertEquals("Small animal", harvest.description)
        assertTrue(harvest.canEdit)
        assertEquals(setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96"), harvest.images.remoteImageIds.toSet())
        assertEquals(47230, harvest.species.knownSpeciesCodeOrNull())
        assertEquals(9, harvest.harvestSpecVersion)
        assertTrue(harvest.harvestReportRequired)
        assertEquals("SENT_FOR_APPROVAL", harvest.harvestReportState.rawBackendEnumValue)
        assertEquals("2022-1-000-10005-7", harvest.permitNumber)
        assertEquals("Poikkeuslupa riistanisäkkäille", harvest.permitType)
        assertEquals("ACCEPTED", harvest.stateAcceptedToHarvestPermit.rawBackendEnumValue)
        var expectedSpecimens = setOf(
            createCommonHarvestSpecimen(
                id = 1078,
                rev = 3,
                gender =  "FEMALE".toBackendEnum(),
                age = "YOUNG".toBackendEnum(),
                weight = 3.45,
                fitnessClass = null,
                antlersType = null,
            )
        )
        assertEquals(1, harvest.amount)
        assertEquals(expectedSpecimens, harvest.specimens.toSet())
        assertNull(harvest.deerHuntingType.rawBackendEnumValue)
        assertNull(harvest.deerHuntingOtherTypeDescription)
        assertEquals(7189988460482656588, harvest.mobileClientRefId)
        assertTrue(harvest.harvestReportDone)

        // Update the harvest
        val updatedHarvestPage = MockHarvestPageData.harvestPageWithUpdatedHarvest
            .deserializeFromJson<HarvestPageDTO>()
            ?.toHarvestPage()
        assertNotNull(updatedHarvestPage)
        runBlocking {
            updater.update(
                username = username,
                harvests = updatedHarvestPage.content,
                overwriteNonModified = false,
            )
        }

        val updatedHarvests = runBlocking {
            repository.listHarvests(username)
        }
        assertEquals(1, updatedHarvests.size)

        harvest = updatedHarvests[0]
        assertEquals(1940, harvest.id)
        assertEquals(6, harvest.rev)
        assertEquals(6900708, harvest.geoLocation.latitude)
        assertEquals(440518, harvest.geoLocation.longitude)
        assertEquals("MANUAL", harvest.geoLocation.source.rawBackendEnumValue)
        assertNull(harvest.geoLocation.accuracy)
        assertNull(harvest.geoLocation.altitude)
        assertNull(harvest.geoLocation.altitudeAccuracy)
        assertEquals(LocalDateTime(2023, 1, 18, 15, 47, 22), harvest.pointOfTime)
        assertEquals("Really small animal", harvest.description)
        assertTrue(harvest.canEdit)
        assertEquals(
            setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96", "a6997f26-dd4f-4687-8145-372365664798"),
            harvest.images.remoteImageIds.toSet()
        )
        assertEquals(47230, harvest.species.knownSpeciesCodeOrNull())
        assertEquals(9, harvest.harvestSpecVersion)
        assertTrue(harvest.harvestReportRequired)
        assertEquals("SENT_FOR_APPROVAL", harvest.harvestReportState.rawBackendEnumValue)
        assertEquals("2022-1-000-10005-7", harvest.permitNumber)
        assertEquals("Poikkeuslupa riistanisäkkäille", harvest.permitType)
        assertEquals("ACCEPTED", harvest.stateAcceptedToHarvestPermit.rawBackendEnumValue)
        expectedSpecimens = setOf(
            createCommonHarvestSpecimen(
                id = 1078,
                rev = 3,
                gender =  "FEMALE".toBackendEnum(),
                age = "YOUNG".toBackendEnum(),
                weight = 3.45,
                fitnessClass = null,
                antlersType = null,
            ),
        )
        assertEquals(2, harvest.amount)
        assertEquals(expectedSpecimens, harvest.specimens.toSet())
        assertNull(harvest.deerHuntingType.rawBackendEnumValue)
        assertNull(harvest.deerHuntingOtherTypeDescription)
        assertEquals(7189988460482656588, harvest.mobileClientRefId)
        assertTrue(harvest.harvestReportDone)
    }

    @Test
    fun testNotModifiedHarvestIsNotWrittenToDatabase() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = HarvestToDatabaseUpdater(database)
        val repository = HarvestRepository(database)

        assertEquals(0, repository.listHarvests(username).size)

        val harvestPage = MockHarvestPageData.harvestPageWithOneHarvest
            .deserializeFromJson<HarvestPageDTO>()
            ?.toHarvestPage()
        assertNotNull(harvestPage)
        runBlocking {
            updater.update(
                username = username,
                harvests = harvestPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedHarvest = repository.listHarvests(username)
        assertEquals(1, insertedHarvest.size)

        var harvest = insertedHarvest[0]
        assertEquals(1, harvest.amount)

        // Change some harvest data but keep revision as it is. Now try to update the harvest => it shouldn't be written to DB
        harvest = harvest.copy(amount = 2)
        updater.update(
            username = username,
            harvests = listOf(harvest),
            overwriteNonModified = false,
        )

        var updatedHarvests = repository.listHarvests(username)
        assertEquals(1, updatedHarvests.size)
        assertEquals(1, updatedHarvests[0].amount)

        // Increase spec version, now it should be written to DB even though revision stays the same
        val newSpecVersion = harvest.harvestSpecVersion + 1
        harvest = harvest.copy(harvestSpecVersion = newSpecVersion)

        updater.update(
            username = username,
            harvests = listOf(harvest),
            overwriteNonModified = false,
        )
        updatedHarvests = repository.listHarvests(username)
        assertEquals(1, updatedHarvests.size)
        assertEquals(2, updatedHarvests[0].amount)
        assertEquals(newSpecVersion, updatedHarvests[0].harvestSpecVersion)
    }

    @Test
    fun `non-modified harvest can be overwritten in database`() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = HarvestToDatabaseUpdater(database)
        val repository = HarvestRepository(database)

        assertEquals(0, repository.listHarvests(username).size)

        val harvestPage = MockHarvestPageData.harvestPageWithOneHarvest
            .deserializeFromJson<HarvestPageDTO>()
            ?.toHarvestPage()
        assertNotNull(harvestPage)
        runBlocking {
            updater.update(
                username = username,
                harvests = harvestPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedHarvest = repository.listHarvests(username)
        assertEquals(1, insertedHarvest.size)

        var harvest = insertedHarvest[0]
        assertEquals(1, harvest.amount)

        // Change some harvest data but keep revision as it is. Now try to update the harvest => it shouldn't be written to DB
        harvest = harvest.copy(amount = 2)
        updater.update(
            username = username,
            harvests = listOf(harvest),
            overwriteNonModified = false,
        )

        var updatedHarvests = repository.listHarvests(username)
        assertEquals(1, updatedHarvests.size)
        assertEquals(1, updatedHarvests[0].amount)

        updater.update(
            username = username,
            harvests = listOf(harvest),
            overwriteNonModified = true,
        )
        updatedHarvests = repository.listHarvests(username)
        assertEquals(1, updatedHarvests.size)
        assertEquals(2, updatedHarvests[0].amount)
    }

    @Test
    fun testImageRemovedOnRemoteIsRemovedAlsoFromLocalImages() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = HarvestRepository(database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.UPLOADED
        )
        val images = harvest.images
            .withNewPrimaryImage(entityImage)
            .withLocalImageUploaded("975d2719-658a-458e-b009-67f6e0f6f1d9")
        val harvestWithUploadedLocalImage = harvest.copy(
            images = images.copy(remoteImageIds = images.remoteImageIds + "975d2719-658a-458e-b009-67f6e0f6f1d9")
        )

        val insertedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvestWithUploadedLocalImage,
        )
        var remoteImages = setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96", "975d2719-658a-458e-b009-67f6e0f6f1d9")
        assertEquals(remoteImages, insertedHarvest.images.remoteImageIds.toSet())
        assertEquals(1, insertedHarvest.images.localImages.size)
        assertEquals(entityImage, insertedHarvest.images.localImages[0])

        // Update harvest with version that doesn't have the local image
        val updatedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvest,
        )
        remoteImages = setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96")
        assertEquals(remoteImages, updatedHarvest.images.remoteImageIds.toSet())
        assertEquals(0, updatedHarvest.images.localImages.size)
    }

    @Test
    fun testImageWithStatusToBeDeletedIsNotSavedToDatabase() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = HarvestRepository(database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.LOCAL_TO_BE_REMOVED
        )
        val harvestWithToBeDeletedImage = harvest.copy(
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(entityImage)
            )
        )

        val insertedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvestWithToBeDeletedImage,
        )

        assertEquals(0, insertedHarvest.images.localImages.size)
    }

    @Test
    fun testHarvestIsMatchedWithMobileClientRefIdWhenRemoteIdIsMissing() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val updater = HarvestToDatabaseUpdater(database)
        val repository = HarvestRepository(database)

        val harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        // Insert harvest without remote id
        val insertedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvest.copy(id = null),
        )

        // Now update with remote id, mobileclientref should be used to match those harvests and
        // only 1 harvest should be in the database after update
        updater.update(
            username = username,
            harvests = listOf(harvest.copy(description = "Updated harvest")),
            overwriteNonModified = false,
        )

        val updatedHarvests = repository.listHarvests(username)
        assertEquals(1, updatedHarvests.size)
        val updatedHarvest = updatedHarvests.first()
        assertEquals(harvest.id, updatedHarvest.id)
        assertEquals("Updated harvest", updatedHarvest.description)
        assertEquals(insertedHarvest.localId, updatedHarvest.localId)
    }

    private fun createCommonHarvestSpecimen(
        id: CommonHarvestSpecimenId?,
        rev: Int?,
        gender: BackendEnum<Gender>,
        age: BackendEnum<GameAge>,
        weight: Double?,
        fitnessClass: GameFitnessClass?,
        antlersType: GameAntlersType?,
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
            fitnessClass = BackendEnum.create(fitnessClass),
            antlersType = BackendEnum.create(antlersType),
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
