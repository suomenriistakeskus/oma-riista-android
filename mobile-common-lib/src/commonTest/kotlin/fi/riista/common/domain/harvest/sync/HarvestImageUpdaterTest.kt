package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.MockHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.toCommonHarvest
import fi.riista.common.domain.model.EntityImage
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

class HarvestImageUpdaterTest {

    @Test
    fun testSendImageToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = HarvestRepository(database)
        val backendApi = BackendAPIMock()
        val commonFileProvider = CommonFileProviderMock(commonFile)
        val updater = harvestImageUpdater(backendApi, database, commonFileProvider)

        var harvest = MockHarvestData.harvest.deserializeFromJson<HarvestDTO>()?.toCommonHarvest()
        assertNotNull(harvest)

        val image = EntityImage(
            serverId = commonFile.fileUuid,
            localIdentifier = null,
            status = EntityImage.Status.LOCAL,
            localUrl = commonFile.path
        )
        harvest = harvest.copy(images = harvest.images.withNewPrimaryImage(image))
        val insertedHarvest = repository.upsertHarvest(
            username = username,
            harvest = harvest,
        )
        var harvestsWithImagesNeedingUpload = repository.getHarvestsWithLocalImages(username).filter { repositoryHarvest ->
            hasLocalNonUploadedImages(repositoryHarvest)
        }
        assertEquals(1, harvestsWithImagesNeedingUpload.size, "before upload")

        updater.updateImagesToBackend(username, harvestsWithImagesNeedingUpload)

        assertEquals(2, backendApi.totalCallCount(), "total backend api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::uploadHarvestImage.name), "upload image api call count")
        assertEquals(1, backendApi.callCount(BackendAPI::deleteHarvestImage.name), "delete image api call count")
        with (backendApi.callParameter(BackendAPI::uploadHarvestImage.name) as BackendAPIMock.UploadImageCallParameters) {
            assertEquals(insertedHarvest.id, eventRemoteId)
            assertEquals(commonFile.fileUuid, uuid)
        }
        assertEquals("86f733ec-8105-48ab-80ce-727dbd1e7a96", backendApi.callParameter(BackendAPI::deleteHarvestImage.name))

        harvestsWithImagesNeedingUpload = repository.getHarvestsWithLocalImages(username).filter { repositoryHarvest ->
            hasLocalNonUploadedImages(repositoryHarvest)
        }
        assertEquals(0, harvestsWithImagesNeedingUpload.size, "after upload")

        val allHarvests = repository.listHarvests(username)
        assertEquals(1, allHarvests.size, "all harvests count")
        val updatedHarvest = allHarvests[0]
        assertEquals(
            expected = listOf(commonFile.fileUuid),
            actual = updatedHarvest.images.remoteImageIds,
            message = "remote image ids"
        )
        assertEquals(1, updatedHarvest.images.localImages.size, "local image count")
        val localImage = updatedHarvest.images.localImages[0]
        assertEquals(EntityImage.Status.UPLOADED, localImage.status)
        assertEquals(commonFile.path, localImage.localUrl)
        assertEquals(commonFile.fileUuid, localImage.serverId)
    }

    private fun hasLocalNonUploadedImages(harvest: CommonHarvest): Boolean {
        val localImages = harvest.images.localImages.filter { image ->
            image.status == EntityImage.Status.LOCAL
        }
        return localImages.isNotEmpty()
    }

    private fun harvestImageUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
        commonFileProvider: CommonFileProvider,
    ): HarvestImageUpdater {
        return HarvestImageUpdater(
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

        override val fileUuid: String = "86f733ec-8105-48ab-80ce-727dbd1e7a97"

        override fun delete() {}
        override fun exists(): Boolean = true
        override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers) {
            // nop
        }
    }
}
