package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.observation.MockObservationData
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.toCommonObservation
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

class ObservationImageUpdaterTest {
    @Test
    fun testSendImageToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendApi = BackendAPIMock()
        val commonFileProvider = CommonFileProviderMock(commonFile)
        val updater = observationImageUpdater(backendApi, database, commonFileProvider)

        var observation = MockObservationData.observation.deserializeFromJson<ObservationDTO>()?.toCommonObservation()
        assertNotNull(observation)

        val image = EntityImage(
            serverId = commonFile.fileUuid,
            localIdentifier = null,
            status = EntityImage.Status.LOCAL,
            localUrl = commonFile.path
        )
        observation = observation.copy(images = observation.images.withNewPrimaryImage(image))
        val insertedObservation = repository.upsertObservation(
            username = username,
            observation = observation,
        )
        var observationsWithImagesNeedingUpload = repository.getObservationsWithLocalImages(username).filter { repositoryObservation ->
            hasLocalNonUploadedImages(repositoryObservation)
        }
        assertEquals(1, observationsWithImagesNeedingUpload.size, "before upload")

        updater.updateImagesToBackend(username, observationsWithImagesNeedingUpload)

        assertEquals(2, backendApi.totalCallCount(), "total backend api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::uploadObservationImage.name), "upload image api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::deleteObservationImage.name), "delete image api call count")
        with (backendApi.callParameter(BackendAPI::uploadObservationImage.name) as BackendAPIMock.UploadImageCallParameters) {
            assertEquals(insertedObservation.remoteId, eventRemoteId)
            assertEquals(commonFile.fileUuid, uuid)
        }
        assertEquals("3d959f28-1bee-4c52-8fb1-ae24383859d0", backendApi.callParameter(BackendAPI::deleteObservationImage.name))

        observationsWithImagesNeedingUpload = repository.getObservationsWithLocalImages(username).filter { repositoryObservation ->
            hasLocalNonUploadedImages(repositoryObservation)
        }
        assertEquals(0, observationsWithImagesNeedingUpload.size, "after upload")

        val allObservations = repository.listObservations(username)
        assertEquals(1, allObservations.size, "all observations count")
        val updatedObservation = allObservations[0]
        assertEquals(
            expected = listOf(commonFile.fileUuid),
            actual = updatedObservation.images.remoteImageIds,
            message = "remote image ids"
        )
        assertEquals(1, updatedObservation.images.localImages.size, "local image count")
        val localImage = updatedObservation.images.localImages[0]
        assertEquals(EntityImage.Status.UPLOADED, localImage.status)
        assertEquals(commonFile.path, localImage.localUrl)
        assertEquals(commonFile.fileUuid, localImage.serverId)
    }

    private fun hasLocalNonUploadedImages(observation: CommonObservation): Boolean {
        val localImages = observation.images.localImages.filter { image ->
            image.status == EntityImage.Status.LOCAL
        }
        return localImages.isNotEmpty()
    }

    private fun observationImageUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
        commonFileProvider: CommonFileProvider,
    ): ObservationImageUpdater {
        return ObservationImageUpdater(
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
