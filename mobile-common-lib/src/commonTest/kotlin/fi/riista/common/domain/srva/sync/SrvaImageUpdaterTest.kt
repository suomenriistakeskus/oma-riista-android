package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.srva.MockSrvaEventData
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.toCommonSrvaEvent
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.io.CommonFile
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.deserializeFromJson
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SrvaImageUpdaterTest {
    @Test
    fun testSendImageToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendApi = BackendAPIMock()
        val commonFileProvider = CommonFileProviderMock(commonFile)
        val updater = srvaImageUpdater(backendApi, database, commonFileProvider)

        var event = MockSrvaEventData.srvaEvent.deserializeFromJson<SrvaEventDTO>()?.toCommonSrvaEvent()
        assertNotNull(event)

        val image = EntityImage(
            serverId = commonFile.fileUuid,
            localIdentifier = null,
            status = EntityImage.Status.LOCAL,
            localUrl = commonFile.path
        )
        event = event.copy(images = event.images.withNewPrimaryImage(image))
        val insertedEvent = repository.upsertSrvaEvent(
            username = username,
            srvaEvent = event,
        )
        var eventsWithImagesNeedingUpload = repository.getEventsWithLocalImages(username).filter { repositoryEvent ->
            hasLocalNonUploadedImages(repositoryEvent)
        }
        assertEquals(1, eventsWithImagesNeedingUpload.size, "before upload")

        updater.updateImagesToBackend(username, eventsWithImagesNeedingUpload)

        assertEquals(2, backendApi.totalCallCount(), "total backend api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::uploadSrvaEventImage.name), "upload image api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::deleteSrvaEventImage.name), "delete image api call count")
        with (backendApi.callParameter(BackendAPI::uploadSrvaEventImage.name) as BackendAPIMock.UploadImageCallParameters) {
            assertEquals(insertedEvent.remoteId, eventRemoteId)
            assertEquals(commonFile.fileUuid, uuid)
        }
        assertEquals("3d959f28-1bee-4c52-8fb1-ae24383859d0", backendApi.callParameter(BackendAPI::deleteSrvaEventImage.name))

        eventsWithImagesNeedingUpload = repository.getEventsWithLocalImages(username).filter { repositoryEvent ->
            hasLocalNonUploadedImages(repositoryEvent)
        }
        assertEquals(0, eventsWithImagesNeedingUpload.size, "after upload")

        val allEvents = repository.listEvents(username)
        assertEquals(1, allEvents.size, "all events count")
        val updatedEvent = allEvents[0]
        assertEquals(
            expected = listOf(commonFile.fileUuid),
            actual = updatedEvent.images.remoteImageIds,
            message = "remote image ids"
        )
        assertEquals(1, updatedEvent.images.localImages.size, "local image count")
        val localImage = updatedEvent.images.localImages[0]
        assertEquals(EntityImage.Status.UPLOADED, localImage.status)
        assertEquals(commonFile.path, localImage.localUrl)
        assertEquals(commonFile.fileUuid, localImage.serverId)
    }

    private fun hasLocalNonUploadedImages(event: CommonSrvaEvent): Boolean {
        val localImages = event.images.localImages.filter {
            image -> image.status == EntityImage.Status.LOCAL
        }
        return localImages.isNotEmpty()
    }

    private fun srvaImageUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
        commonFileProvider: CommonFileProvider,
        ): SrvaImageUpdater {
        return SrvaImageUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
            commonFileProvider = commonFileProvider,
        )
    }

    private val commonFile = object : CommonFile {
        override val path: String
            get() = "/images/$fileUuid"

        override val fileUuid: String = "e4f6c365-7a48-447b-aca3-2cedc5c0c2d3"

        override fun delete() {}
        override fun exists(): Boolean = true
        override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers) {
            // nop
        }
    }
}
