package fi.riista.common.domain.srva.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.io.CommonFile
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.contains

internal class SrvaImageUpdater(
    backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    private val commonFileProvider: CommonFileProvider,
) : BackendApiProvider by backendApiProvider {
    private val repository = SrvaEventRepository(database)

    /**
     * Updates the images from given [srvaEvents] to the backend.
     *
     * Prerequisites for updating to be performed:
     * - srva event has been uploaded to backend
     * - images won't be updated unless a new local image has been added i.e. there must be a local [EntityImage]
     *   with status == [EntityImage.Status.LOCAL].
     */
    suspend fun updateImagesToBackend(username: String, srvaEvents: List<CommonSrvaEvent>) {
        val srvaEventsWithNewLocalImages = srvaEvents.filter { srvaEvent ->
            // It is not possible to upload images for events that are not yet sent to backend
            srvaEvent.remoteId != null &&
                    srvaEvent.images.localImages.contains { it.status == EntityImage.Status.LOCAL }
        }

        srvaEventsWithNewLocalImages.forEach {
            updateImagesToBackend(username = username, srvaEvent = it)
        }
    }

    suspend fun updateImagesToBackend(username: String, srvaEvent: CommonSrvaEvent) {
        if (srvaEvent.remoteId == null) {
            logger.d { "Refusing to update srvaEvent ${srvaEvent.localId} images to backend. No remote id." }
            return
        }

        val imageToSend: EntityImage = srvaEvent.images.localImages.firstOrNull { it.status == EntityImage.Status.LOCAL }
            ?: kotlin.run {
                logger.d { "Refusing to update srvaEvent ${srvaEvent.localId} images to backend. No local image." }
                return
            }

        val imagesToRemove = srvaEvent.images.remoteImageIds.filter { imageId ->
            // just in case image was already in remoteImageIds (should not happen)
            imageId != imageToSend.serverId
        }

        val (imageWasSent, sentImage) = if (uploadImageFile(srvaEventRemoteId = srvaEvent.remoteId, image = imageToSend)) {
            // update status
            true to imageToSend.copy(status = EntityImage.Status.UPLOADED)
        } else {
            // uploading failed, don't change status as that allows attempting again next time
            false to imageToSend
        }

        // Don't lose images that we failed to delete. It is then possible to try deleting them next time.
        val remainingImages = imagesToRemove.filter { imageUuid ->
            val imageWasDeleted = deleteSrvaEventImage(imageUuid = imageUuid)
            imageWasDeleted.not()
        } + listOfNotNull(
            sentImage.serverId.takeIf { imageWasSent }
        )

        val updatedSrvaEvent = srvaEvent.copy(
            images = EntityImages(
                remoteImageIds = remainingImages,
                localImages = listOf(sentImage)
            )
        )

        try {
            repository.upsertSrvaEvent(username, updatedSrvaEvent)
        } catch (e: Exception) {
            RiistaSDK.crashlyticsLogger.log(
                e,
                "Unable to save SRVA to DB after images updated. remoteId=${updatedSrvaEvent.remoteId}"
            )
        }
    }

    private suspend fun uploadImageFile(srvaEventRemoteId: Long, image: EntityImage): Boolean {
        if (image.serverId == null) {
            logger.w { "Unable to upload SRVA image for event $srvaEventRemoteId as serverId == null" }
            return false
        }

        // Search image file both from local images and from temporary images. Depending on selected sync (automatic
        // or manual) the image may be in either of those!
        // - automatic: image is probably still in temporary files as it will most likely be copied once event
        //              has been saved (and uploaded) successfully. More common case -> this is first in list
        // - manual: event is saved and image is moved to local images after saving. Sync is performed afterwards.
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
            logger.w { "Unable to upload SRVA image ${image.serverId}, as file is not found" }
            return false
        }

        val response = backendAPI.uploadSrvaEventImage(
            eventRemoteId = srvaEventRemoteId,
            uuid = image.serverId,
            contentType = "image/jpeg", // TODO: Should we sent the real contentType? This one copied from old implementation
            file = file
        )
        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to upload srva (remoteId = $srvaEventRemoteId) image (id = ${image.serverId}) to backend. Status code $statusCode" }
        }
        return false
    }

    private suspend fun deleteSrvaEventImage(imageUuid: String): Boolean {
        val response = backendAPI.deleteSrvaEventImage(imageUuid = imageUuid)

        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to delete srva image (id = $imageUuid). Status code $statusCode" }
        }
        return false
    }

    companion object {
        private val logger by getLogger(SrvaImageUpdater::class)
    }
}
