package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.srva.MockSrvaEventData
import fi.riista.common.domain.srva.MockSrvaEventPageData
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaMethod
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventPageDTO
import fi.riista.common.domain.srva.sync.dto.toCommonSrvaEvent
import fi.riista.common.domain.srva.sync.dto.toSrvaEventPage
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SrvaEventToDatabaseUpdaterTest {

    @Test
    fun testInsertAndUpdateEvent() {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = SrvaEventToDatabaseUpdater(database)
        val repository = SrvaEventRepository(database)

        assertEquals(0, repository.listEvents(username).size)

        val eventPage = MockSrvaEventPageData.srvaPageWithOneEvent.deserializeFromJson<SrvaEventPageDTO>()?.toSrvaEventPage()
        assertNotNull(eventPage)
        runBlocking {
            updater.update(
                username = username,
                srvaEvents = eventPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedEvents = repository.listEvents(username)
        assertEquals(1, insertedEvents.size)

        var event = insertedEvents[0]
        assertEquals(19, event.remoteId)
        assertEquals(1, event.revision)
        assertEquals(7034880, event.location.latitude)
        assertEquals(530720, event.location.longitude)
        assertEquals("MANUAL", event.location.source.rawBackendEnumValue)
        assertEquals(0.8, event.location.accuracy)
        assertEquals(21.4, event.location.altitude)
        assertEquals(0.6, event.location.altitudeAccuracy)
        assertEquals(LocalDateTime(2016, 2, 18, 11, 0, 0), event.pointOfTime)
        assertEquals("Some SRVA event", event.description)
        assertTrue(event.canEdit)
        assertEquals(setOf("3d959f28-1bee-4c52-8fb1-ae24383859d0"), event.images.remoteImageIds.toSet())
        assertEquals("ACCIDENT", event.eventCategory.rawBackendEnumValue)
        assertEquals("TRAFFIC_ACCIDENT", event.eventType.rawBackendEnumValue)
        var expectedMethods = setOf(
            CommonSrvaMethod(type = SrvaMethodType.TRACED_WITH_DOG.toBackendEnum(), selected = true),
            CommonSrvaMethod(type = SrvaMethodType.TRACED_WITHOUT_DOG.toBackendEnum(), selected = false),
            CommonSrvaMethod(type = SrvaMethodType.OTHER.toBackendEnum(), selected = false),
        )
        assertEquals(expectedMethods, event.methods.toSet())
        assertEquals(99, event.personCount)
        assertEquals(88, event.hoursSpent)
        assertEquals("ANIMAL_FOUND_AND_NOT_TERMINATED", event.eventResult.rawBackendEnumValue)
        assertEquals(4, event.author?.id)
        assertEquals(15, event.author?.revision)
        assertEquals("Pena", event.author?.byName)
        assertEquals("Mujunen", event.author?.lastName)
        assertEquals(1, event.specimens.size)
        var specimen = event.specimens[0]
        assertEquals("YOUNG", specimen.age.rawBackendEnumValue)
        assertEquals("FEMALE", specimen.gender.rawBackendEnumValue)
        assertEquals(209, event.rhyId)
        assertEquals("REJECTED", event.state.rawBackendEnumValue)
        assertEquals("Pentti", event.approver?.firstName)
        assertEquals("Mujunen", event.approver?.lastName)
        assertEquals(2, event.srvaSpecVersion)

        // Update the event
        val updatedEventPage = MockSrvaEventPageData.srvaPageWithUpdatedEvent.deserializeFromJson<SrvaEventPageDTO>()?.toSrvaEventPage()
        assertNotNull(updatedEventPage)
        runBlocking {
            updater.update(
                username = username,
                srvaEvents = updatedEventPage.content,
                overwriteNonModified = false,
            )
        }

        val updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)

        event = updatedEvents[0]
        assertEquals(19, event.remoteId)
        assertEquals(2, event.revision)
        assertEquals(7034882, event.location.latitude)
        assertEquals(530722, event.location.longitude)
        assertEquals("MANUAL", event.location.source.rawBackendEnumValue)
        assertEquals(0.9, event.location.accuracy)
        assertEquals(21.5, event.location.altitude)
        assertEquals(0.7, event.location.altitudeAccuracy)
        assertEquals(LocalDateTime(2016, 2, 18, 12, 1, 20), event.pointOfTime)
        assertEquals("Some SRVA event2", event.description)
        assertFalse(event.canEdit)
        assertEquals(
            setOf("3d959f28-1bee-4c52-8fb1-ae24383859d0", "3d959f28-1bee-4c52-8fb1-ae24383859d2"),
            event.images.remoteImageIds.toSet()
        )
        assertEquals("ACCIDENT", event.eventCategory.rawBackendEnumValue)
        assertEquals("TRAFFIC_ACCIDENT", event.eventType.rawBackendEnumValue)
        expectedMethods = setOf(
            CommonSrvaMethod(type = SrvaMethodType.TRACED_WITH_DOG.toBackendEnum(), selected = false),
            CommonSrvaMethod(type = SrvaMethodType.TRACED_WITHOUT_DOG.toBackendEnum(), selected = true),
            CommonSrvaMethod(type = SrvaMethodType.OTHER.toBackendEnum(), selected = false),
        )
        assertEquals(expectedMethods, event.methods.toSet())
        assertEquals(98, event.personCount)
        assertEquals(87, event.hoursSpent)
        assertEquals("ANIMAL_FOUND_AND_NOT_TERMINATED", event.eventResult.rawBackendEnumValue)
        assertEquals(4, event.author?.id)
        assertEquals(16, event.author?.revision)
        assertEquals("Pentikäinen", event.author?.byName)
        assertEquals("Mujunen", event.author?.lastName)
        assertEquals(1, event.specimens.size)
        specimen = event.specimens[0]
        assertEquals("ADULT", specimen.age.rawBackendEnumValue)
        assertEquals("MALE", specimen.gender.rawBackendEnumValue)
        assertEquals(20, event.rhyId)
        assertEquals("ACCEPTED", event.state.rawBackendEnumValue)
        assertEquals("Rauno", event.approver?.firstName)
        assertEquals("Koskinen", event.approver?.lastName)
        assertEquals(123890, event.mobileClientRefId)
        assertEquals(2, event.srvaSpecVersion)
        assertEquals(false, event.modified)
    }

    @Test
    fun testNotModifiedEventIsNotWrittenToDatabase() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = SrvaEventToDatabaseUpdater(database)
        val repository = SrvaEventRepository(database)

        assertEquals(0, repository.listEvents(username).size)

        val eventPage = MockSrvaEventPageData.srvaPageWithOneEvent.deserializeFromJson<SrvaEventPageDTO>()?.toSrvaEventPage()
        assertNotNull(eventPage)
        runBlocking {
            updater.update(
                username = username,
                srvaEvents = eventPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedEvents = repository.listEvents(username)
        assertEquals(1, insertedEvents.size)

        var event = insertedEvents[0]
        assertEquals(88, event.hoursSpent)

        // Change some event data but keep revision as it is. Now try to update the event => it shouldn't be written to DB
        event = event.copy(hoursSpent = 1)
        updater.update(
            username = username,
            srvaEvents = listOf(event),
            overwriteNonModified = false,
        )

        var updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)
        assertEquals(88, updatedEvents[0].hoursSpent)

        // Increase spec version, now it should be written to DB even though revision stays the same
        val newSpecVersion = event.srvaSpecVersion + 1
        event = event.copy(srvaSpecVersion = newSpecVersion)

        updater.update(
            username = username,
            srvaEvents = listOf(event),
            overwriteNonModified = false,
        )
        updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)
        assertEquals(1, updatedEvents[0].hoursSpent)
        assertEquals(newSpecVersion, updatedEvents[0].srvaSpecVersion)
    }

    @Test
    fun `non-modified srva can be overwritten in database`() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = SrvaEventToDatabaseUpdater(database)
        val repository = SrvaEventRepository(database)

        assertEquals(0, repository.listEvents(username).size)

        val eventPage = MockSrvaEventPageData.srvaPageWithOneEvent.deserializeFromJson<SrvaEventPageDTO>()?.toSrvaEventPage()
        assertNotNull(eventPage)
        runBlocking {
            updater.update(
                username = username,
                srvaEvents = eventPage.content,
                overwriteNonModified = false,
            )
        }

        val insertedEvents = repository.listEvents(username)
        assertEquals(1, insertedEvents.size)

        var event = insertedEvents[0]
        assertEquals(88, event.hoursSpent)

        // Change some event data but keep revision as it is. Now try to update the event => it shouldn't be written to DB
        event = event.copy(hoursSpent = 1)
        updater.update(
            username = username,
            srvaEvents = listOf(event),
            overwriteNonModified = false,
        )

        var updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)
        assertEquals(88, updatedEvents[0].hoursSpent)

        updater.update(
            username = username,
            srvaEvents = listOf(event),
            overwriteNonModified = true,
        )
        updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)
        assertEquals(1, updatedEvents[0].hoursSpent)
    }

    @Test
    fun testImageRemovedOnRemoteIsRemovedAlsoFromLocalImages() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)

        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)

        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.UPLOADED
        )
        val images = event.images
            .withNewPrimaryImage(entityImage)
            .withLocalImageUploaded("975d2719-658a-458e-b009-67f6e0f6f1d9")
        val eventWithUploadedLocalImage = event.copy(
            images = images.copy(remoteImageIds = images.remoteImageIds + "975d2719-658a-458e-b009-67f6e0f6f1d9")
        )

        val insertedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = eventWithUploadedLocalImage,
        )
        var remoteImages = setOf("3d959f28-1bee-4c52-8fb1-ae24383859d0", "975d2719-658a-458e-b009-67f6e0f6f1d9")
        assertEquals(remoteImages, insertedEvent.images.remoteImageIds.toSet())
        assertEquals(1, insertedEvent.images.localImages.size)
        assertEquals(entityImage, insertedEvent.images.localImages[0])

        // Update event with version that doesn't have the local image
        val updatedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event,
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
        val repository = SrvaEventRepository(database)

        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
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

        val insertedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = eventWithToBeDeletedImage,
        )

        assertEquals(0, insertedEvent.images.localImages.size)
    }

    @Test
    fun testEventIsMatchedWithMobileClientRefIdWhenRemoteIdIsMissing() = runBlocking {
        val username = "user"
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val updater = SrvaEventToDatabaseUpdater(database)
        val repository = SrvaEventRepository(database)

        val event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)

        // Add an local image that is not yet sent to backend. It must not be removed in update.
        val entityImage = EntityImage(
            serverId = "975d2719-658a-458e-b009-67f6e0f6f1d9",
            localIdentifier = null,
            localUrl = "/data/user/0/fi.riista.mobile.dev/files/images/975d2719-658a-458e-b009-67f6e0f6f1d9",
            status = EntityImage.Status.LOCAL
        )
        val eventWithLocalImage = event.copy(
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(entityImage)
            )
        )

        val insertedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = eventWithLocalImage.copy(remoteId = null),
        )

        updater.update(
            username = username,
            srvaEvents = listOf(event),
            overwriteNonModified = false,
        )
        val updatedEvents = repository.listEvents(username)
        assertEquals(1, updatedEvents.size)
        val updatedEvent = updatedEvents.first()
        assertEquals(event.remoteId, updatedEvent.remoteId)
        assertEquals(insertedEvent.localId, updatedEvent.localId)
        assertEquals(insertedEvent.images.localImages, updatedEvent.images.localImages)
    }
}
