package fi.riista.common.domain.harvest.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.io.CommonFile
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.contains

internal class HarvestImageUpdater(
    backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    private val commonFileProvider: CommonFileProvider,
) : BackendApiProvider by backendApiProvider {
    private val repository = HarvestRepository(database)

    /**
     * Updates the images from given [harvests] to the backend.
     *
     * Prerequisites for updating to be performed:
     * - harvest has been uploaded to backend
     * - images won't be updated unless a new local image has been added i.e. there must be a local [EntityImage]
     *   with status == [EntityImage.Status.LOCAL].
     */
    suspend fun updateImagesToBackend(username: String, harvests: List<CommonHarvest>) {
        val harvestsWithNewLocalImages = harvests.filter { harvest ->
            // It is not possible to upload images for harvests that are not yet sent to backend
            harvest.id != null &&
                    harvest.images.localImages.contains { it.status == EntityImage.Status.LOCAL }
        }

        harvestsWithNewLocalImages.forEach {
            updateImagesToBackend(username = username, harvest = it)
        }
    }

    suspend fun updateImagesToBackend(username: String, harvest: CommonHarvest) {
        if (harvest.id == null) {
            logger.d { "Refusing to update harvest ${harvest.localId} images to backend. No remote id." }
            return
        }

        val imageToSend: EntityImage = harvest.images.localImages.firstOrNull { it.status == EntityImage.Status.LOCAL }
            ?: kotlin.run {
                logger.d { "Refusing to update harvest ${harvest.localId} images to backend. No local image." }
                return
            }

        val imagesToRemove = harvest.images.remoteImageIds.filter { imageId ->
            // just in case image was already in remoteImageIds (should not happen)
            imageId != imageToSend.serverId
        }

        val (imageWasSent, sentImage) = if (uploadImageFile(harvestRemoteId = harvest.id, image = imageToSend)) {
            // update status
            true to imageToSend.copy(status = EntityImage.Status.UPLOADED)
        } else {
            // uploading failed, don't change status as that allows attempting again next time
            false to imageToSend
        }

        // Don't lose images that we failed to delete. It is then possible to try deleting them next time.
        val remainingImages = imagesToRemove.filter { imageUuid ->
            val imageWasDeleted = deleteHarvestImage(imageUuid = imageUuid)
            imageWasDeleted.not()
        } + listOfNotNull(
            sentImage.serverId.takeIf { imageWasSent }
        )

        val updatedHarvest = harvest.copy(
            images = EntityImages(
                remoteImageIds = remainingImages,
                localImages = listOf(sentImage)
            )
        )

        try {
            repository.upsertHarvest(username, updatedHarvest)
        } catch (e: Exception) {
            RiistaSDK.crashlyticsLogger.log(
                e,
                "Unable to save harvest to DB after images updated. remoteId=${updatedHarvest.id}"
            )
        }
    }

    private suspend fun uploadImageFile(harvestRemoteId: Long, image: EntityImage): Boolean {
        if (image.serverId == null) {
            logger.w { "Unable to upload image for harvest $harvestRemoteId as serverId == null" }
            return false
        }

        // Search image file both from local images and from temporary images. Depending on selected sync (automatic
        // or manual) the image may be in either of those!
        // - automatic: image is probably still in temporary files as it will most likely be copied once harvest
        //              has been saved (and uploaded) successfully. More common case -> this is first in list
        // - manual: harvest is saved and image is moved to local images after saving. Sync is performed afterwards.
        val searchDirectories = listOf(
            CommonFileProvider.Directory.TEMPORARY_FILES,
            CommonFileProvider.Directory.LOCAL_IMAGES,
        )

        val file: CommonFile? = searchDirectories.firstNotNullOfOrNull { directory ->
            val candidateFile = commonFileProvider.getFile(directory, fileUuid = image.serverId)
            if (candidateFile?.exists() == true) {
                candidateFile
            } else {
                null
            }
        }

        if (file == null) {
            logger.w { "Unable to upload harvest image ${image.serverId}, as file is not found" }
            return false
        }

        val response = backendAPI.uploadHarvestImage(
            harvestRemoteId = harvestRemoteId,
            uuid = image.serverId,
            contentType = "image/jpeg", // TODO: Should we sent the real contentType? This one copied from old implementation
            file = file
        )
        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to upload harvest (remoteId = $harvestRemoteId) image (id = ${image.serverId}) to backend. Status code $statusCode" }
        }
        return false
    }

    private suspend fun deleteHarvestImage(imageUuid: String): Boolean {
        val response = backendAPI.deleteHarvestImage(imageUuid = imageUuid)

        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to delete harvest image (id = $imageUuid). Status code $statusCode" }
        }
        return false
    }

    companion object {
        private val logger by getLogger(HarvestImageUpdater::class)
    }
}
