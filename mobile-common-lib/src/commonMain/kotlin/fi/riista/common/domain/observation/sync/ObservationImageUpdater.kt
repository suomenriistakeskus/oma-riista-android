package fi.riista.common.domain.observation.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.io.CommonFile
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.contains

internal class ObservationImageUpdater(
    backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    private val commonFileProvider: CommonFileProvider,
) : BackendApiProvider by backendApiProvider {
    private val repository = ObservationRepository(database)

    /**
     * Updates the images from given [observations] to the backend.
     *
     * Prerequisites for updating to be performed:
     * - observation has been uploaded to backend
     * - images won't be updated unless a new local image has been added i.e. there must be a local [EntityImage]
     *   with status == [EntityImage.Status.LOCAL].
     */
    suspend fun updateImagesToBackend(username: String, observations: List<CommonObservation>) {
        val observationsWithNewLocalImages = observations.filter { observation ->
            // It is not possible to upload images for observations that are not yet sent to backend
            observation.remoteId != null &&
                    observation.images.localImages.contains { it.status == EntityImage.Status.LOCAL }
        }

        observationsWithNewLocalImages.forEach {
            updateImagesToBackend(username = username, observation = it)
        }
    }

    suspend fun updateImagesToBackend(username: String, observation: CommonObservation) {
        if (observation.remoteId == null) {
            logger.d { "Refusing to update observation ${observation.localId} images to backend. No remote id." }
            return
        }

        val imageToSend: EntityImage = observation.images.localImages.firstOrNull { it.status == EntityImage.Status.LOCAL }
            ?: kotlin.run {
                logger.d { "Refusing to update observation ${observation.localId} images to backend. No local image." }
                return
            }

        val imagesToRemove = observation.images.remoteImageIds.filter { imageId ->
            // just in case image was already in remoteImageIds (should not happen)
            imageId != imageToSend.serverId
        }

        val (imageWasSent, sentImage) = if (uploadImageFile(observationRemoteId = observation.remoteId, image = imageToSend)) {
            // update status
            true to imageToSend.copy(status = EntityImage.Status.UPLOADED)
        } else {
            // uploading failed, don't change status as that allows attempting again next time
            false to imageToSend
        }

        // Don't lose images that we failed to delete. It is then possible to try deleting them next time.
        val remainingImages = imagesToRemove.filter { imageUuid ->
            val imageWasDeleted = deleteObservationImage(imageUuid = imageUuid)
            imageWasDeleted.not()
        } + listOfNotNull(
            sentImage.serverId.takeIf { imageWasSent }
        )

        val updatedObservation = observation.copy(
            images = EntityImages(
                remoteImageIds = remainingImages,
                localImages = listOf(sentImage)
            )
        )

        try {
            repository.upsertObservation(username, updatedObservation)
        } catch (e: Exception) {
            RiistaSDK.crashlyticsLogger.log(
                e,
                "Unable to save observation to DB after images updated. remoteId=${updatedObservation.remoteId}"
            )
        }
    }

    private suspend fun uploadImageFile(observationRemoteId: Long, image: EntityImage): Boolean {
        if (image.serverId == null) {
            logger.w { "Unable to upload image for observation $observationRemoteId as serverId == null" }
            return false
        }

        // Search image file both from local images and from temporary images. Depending on selected sync (automatic
        // or manual) the image may be in either of those!
        // - automatic: image is probably still in temporary files as it will most likely be copied once observation
        //              has been saved (and uploaded) successfully. More common case -> this is first in list
        // - manual: observation is saved and image is moved to local images after saving. Sync is performed afterwards.
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
            logger.w { "Unable to upload observation image ${image.serverId}, as file is not found" }
            return false
        }

        val response = backendAPI.uploadObservationImage(
            observationRemoteId = observationRemoteId,
            uuid = image.serverId,
            contentType = "image/jpeg", // TODO: Should we sent the real contentType? This one copied from old implementation
            file = file
        )
        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to upload observation (remoteId = $observationRemoteId) image (id = ${image.serverId}) to backend. Status code $statusCode" }
        }
        return false
    }

    private suspend fun deleteObservationImage(imageUuid: String): Boolean {
        val response = backendAPI.deleteObservationImage(imageUuid = imageUuid)

        response.onSuccessWithoutData {
            return true
        }

        response.onError { statusCode, _ ->
            logger.w { "Failed to delete observation image (id = $imageUuid). Status code $statusCode" }
        }
        return false
    }

    companion object {
        private val logger by getLogger(ObservationImageUpdater::class)
    }
}
