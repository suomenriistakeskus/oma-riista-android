package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.observation.MockObservationData
import fi.riista.common.domain.observation.MockObservationPageData
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.ObservationPageDTO
import fi.riista.common.domain.observation.sync.dto.toCommonObservation
import fi.riista.common.domain.observation.sync.dto.toObservationPage
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObservationToDatabaseUpdaterTest {

    @Test
    fun testInsertAndUpdateObservation() {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = ObservationToDatabaseUpdater(database)
        val repository = ObservationRepository(database)

        assertEquals(0, repository.listObservations(username).size)

        val observationPage = MockObservationPageData.observationPageWithOneObservation
            .deserializeFromJson<ObservationPageDTO>()
            ?.toObservationPage()
        assertNotNull(observationPage)
        runBlocking {
            updater.update(
                username = username,
                observations = observationPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedObservations = repository.listObservations(username)
        assertEquals(1, insertedObservations.size)

        var observation = insertedObservations[0]
        assertEquals(330, observation.remoteId)
        assertEquals(1, observation.revision)
        assertEquals(7035076, observation.location.latitude)
        assertEquals(483517, observation.location.longitude)
        assertEquals("GPS_DEVICE", observation.location.source.rawBackendEnumValue)
        assertEquals(12.302000045776367, observation.location.accuracy)
        assertEquals(21.4, observation.location.altitude)
        assertEquals(0.6, observation.location.altitudeAccuracy)
        assertEquals(LocalDateTime(2020, 1, 30, 15, 17, 34), observation.pointOfTime)
        assertEquals("This is description", observation.description)
        assertTrue(observation.canEdit)
        assertEquals(setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96"), observation.images.remoteImageIds.toSet())
        assertEquals(26440, observation.species.knownSpeciesCodeOrNull())
        assertEquals("ULOSTE", observation.observationType.rawBackendEnumValue)
        assertEquals("NORMAL", observation.observationCategory.rawBackendEnumValue)
        assertEquals(12, observation.inYardDistanceToResidence)
        assertFalse(assertNotNull(observation.verifiedByCarnivoreAuthority))
        assertEquals("Matti", observation.observerName)
        assertEquals("123456", observation.observerPhoneNumber)
        assertEquals("Other info", observation.officialAdditionalInfo)
        var expectedSpecimens = listOf(
            CommonObservationSpecimen(
                remoteId = 192,
                revision = 0,
                gender =  "FEMALE".toBackendEnum(),
                age = "ADULT".toBackendEnum(),
                stateOfHealth =  "HEALTHY".toBackendEnum(),
                marking =  "COLLAR_OR_RADIO_TRANSMITTER".toBackendEnum(),
                widthOfPaw =  5.5,
                lengthOfPaw =  6.6
            )
        )
        assertEquals(expectedSpecimens, observation.specimens)
        assertFalse(assertNotNull(observation.pack))
        assertFalse(assertNotNull(observation.litter))
        assertEquals(6787724453918204169, observation.mobileClientRefId)
        assertEquals(4, observation.observationSpecVersion)
        assertEquals(1, observation.totalSpecimenAmount)

        // Update the observation
        val updatedObservationPage = MockObservationPageData.observationPageWithUpdatedObservation
            .deserializeFromJson<ObservationPageDTO>()
            ?.toObservationPage()
        println(updatedObservationPage)
        assertNotNull(updatedObservationPage)
        runBlocking {
            updater.update(
                username = username,
                observations = updatedObservationPage.content,
                overwriteNonModified = false,
            )
        }

        val updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)

        observation = updatedObservations[0]
        assertEquals(330, observation.remoteId)
        assertEquals(2, observation.revision)
        assertEquals(7035077, observation.location.latitude)
        assertEquals(483518, observation.location.longitude)
        assertEquals("GPS_DEVICE", observation.location.source.rawBackendEnumValue)
        assertEquals(LocalDateTime(2020, 3, 30, 15, 17, 34), observation.pointOfTime)
        assertEquals("This is updated description", observation.description)
        assertFalse(observation.canEdit)
        assertEquals(
            setOf("86f733ec-8105-48ab-80ce-727dbd1e7a96", "a6997f26-dd4f-4687-8145-372365664798"),
            observation.images.remoteImageIds.toSet()
        )
        assertEquals(26441, observation.species.knownSpeciesCodeOrNull())
        assertEquals("NAKO", observation.observationType.rawBackendEnumValue)
        assertEquals("NORMAL", observation.observationCategory.rawBackendEnumValue)
        assertEquals(13, observation.inYardDistanceToResidence)
        assertTrue(assertNotNull(observation.verifiedByCarnivoreAuthority))
        assertEquals("Ville", observation.observerName)
        assertEquals("7654321", observation.observerPhoneNumber)
        assertEquals("New info", observation.officialAdditionalInfo)
        expectedSpecimens = listOf(
            CommonObservationSpecimen(
                remoteId = 192,
                revision = 1,
                gender =  "MALE".toBackendEnum(),
                age = "YOUNG".toBackendEnum(),
                stateOfHealth =  "HEALTHY".toBackendEnum(),
                marking =  "COLLAR_OR_RADIO_TRANSMITTER".toBackendEnum(),
                widthOfPaw =  5.4,
                lengthOfPaw =  6.5
            ),
        )
        assertEquals(expectedSpecimens, observation.specimens)
        assertTrue(assertNotNull(observation.pack))
        assertTrue(assertNotNull(observation.litter))
        assertEquals(6787724453918204169, observation.mobileClientRefId)
        assertEquals(4, observation.observationSpecVersion)
        assertEquals(2, observation.totalSpecimenAmount)
    }

    @Test
    fun testNotModifiedObservationIsNotWrittenToDatabase() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = ObservationToDatabaseUpdater(database)
        val repository = ObservationRepository(database)

        assertEquals(0, repository.listObservations(username).size)

        val observationPage = MockObservationPageData.observationPageWithOneObservation
            .deserializeFromJson<ObservationPageDTO>()
            ?.toObservationPage()
        assertNotNull(observationPage)
        runBlocking {
            updater.update(
                username = username,
                observations = observationPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedObservations = repository.listObservations(username)
        assertEquals(1, insertedObservations.size)

        var observation = insertedObservations[0]
        assertEquals(1, observation.totalSpecimenAmount)

        // Change some observation data but keep revision as it is. Now try to update the observation => it shouldn't be written to DB
        observation = observation.copy(totalSpecimenAmount = 2)
        updater.update(
            username = username,
            observations = listOf(observation),
            overwriteNonModified = false,
        )

        var updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)
        assertEquals(1, updatedObservations[0].totalSpecimenAmount)

        // Increase spec version, now it should be written to DB even though revision stays the same
        val newSpecVersion = observation.observationSpecVersion + 1
        observation = observation.copy(observationSpecVersion = newSpecVersion)

        updater.update(
            username = username,
            observations = listOf(observation),
            overwriteNonModified = false,
        )
        updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)
        assertEquals(2, updatedObservations[0].totalSpecimenAmount)
        assertEquals(newSpecVersion, updatedObservations[0].observationSpecVersion)
    }

    @Test
    fun `non-modified observation can be overwritten in database`() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = ObservationToDatabaseUpdater(database)
        val repository = ObservationRepository(database)

        assertEquals(0, repository.listObservations(username).size)

        val observationPage = MockObservationPageData.observationPageWithOneObservation
            .deserializeFromJson<ObservationPageDTO>()
            ?.toObservationPage()
        assertNotNull(observationPage)
        runBlocking {
            updater.update(
                username = username,
                observations = observationPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedObservations = repository.listObservations(username)
        assertEquals(1, insertedObservations.size)

        var observation = insertedObservations[0]
        assertEquals(1, observation.totalSpecimenAmount)

        // Change some observation data but keep revision as it is. Now try to update the observation => it shouldn't be written to DB
        observation = observation.copy(totalSpecimenAmount = 2)
        updater.update(
            username = username,
            observations = listOf(observation),
            overwriteNonModified = false,
        )

        var updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)
        assertEquals(1, updatedObservations[0].totalSpecimenAmount)

        updater.update(
            username = username,
            observations = listOf(observation),
            overwriteNonModified = true,
        )
        updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)
        assertEquals(2, updatedObservations[0].totalSpecimenAmount)
    }

    @Test
    fun testImageRemovedOnRemoteIsRemovedAlsoFromLocalImages() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)

        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)

        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.UPLOADED
        )
        val images = observation.images
            .withNewPrimaryImage(entityImage)
            .withLocalImageUploaded("975d2719-658a-458e-b009-67f6e0f6f1d9")
        val observationWithUploadedLocalImage = observation.copy(
            images = images.copy(remoteImageIds = images.remoteImageIds + "975d2719-658a-458e-b009-67f6e0f6f1d9")
        )

        val insertedEvent = repository.upsertObservation(
            username = username,
            observation = observationWithUploadedLocalImage,
        )
        var remoteImages = setOf("3d959f28-1bee-4c52-8fb1-ae24383859d0", "975d2719-658a-458e-b009-67f6e0f6f1d9")
        assertEquals(remoteImages, insertedEvent.images.remoteImageIds.toSet())
        assertEquals(1, insertedEvent.images.localImages.size)
        assertEquals(entityImage, insertedEvent.images.localImages[0])

        // Update observation with version that doesn't have the local image
        val updatedEvent = repository.upsertObservation(
            username = username,
            observation = observation,
        )
        remoteImages = setOf("3d959f28-1bee-4c52-8fb1-ae24383859d0")
        assertEquals(remoteImages, updatedEvent.images.remoteImageIds.toSet())
        assertEquals(0, updatedEvent.images.localImages.size)
    }

    @Test
    fun testImageWithStatusToBeDeletedIsNotSavedToDatabase() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = ObservationRepository(database)

        val event = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(event)

        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.LOCAL_TO_BE_REMOVED
        )
        val eventWithToBeDeletedImage = event.copy(
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(entityImage)
            )
        )

        val insertedEvent = repository.upsertObservation(
            username = username,
            observation = eventWithToBeDeletedImage,
        )

        assertEquals(0, insertedEvent.images.localImages.size)
    }

    @Test
    fun testEventIsMatchedWithMobileClientRefIdWhenRemoteIdIsMissing() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val updater = ObservationToDatabaseUpdater(database)
        val repository = ObservationRepository(database)

        val observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)

        // Add an local image that is not yet sent to backend. It must not be removed in update.
        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.LOCAL
        )
        val observationWithLocalImage = observation.copy(
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(entityImage)
            )
        )

        val insertedObservation = repository.upsertObservation(
            username = username,
            observation = observationWithLocalImage.copy(remoteId = null),
        )

        updater.update(
            username = username,
            observations = listOf(observation),
            overwriteNonModified = false,
        )
        val updatedObservations = repository.listObservations(username)
        assertEquals(1, updatedObservations.size)
        val updatedObservation = updatedObservations.first()
        assertEquals(observation.remoteId, updatedObservation.remoteId)
        assertEquals(insertedObservation.localId, updatedObservation.localId)
        assertEquals(insertedObservation.images.localImages, updatedObservation.images.localImages)
    }
}
