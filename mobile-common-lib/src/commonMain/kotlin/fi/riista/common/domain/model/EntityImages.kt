package fi.riista.common.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EntityImages(
    /**
     * The ids of the images that have been uploaded to backend. The image ids should be in
     * the order they were originally uploaded i.e. the id of the last uploaded image should
     * be the last in the list.
     */
    val remoteImageIds: List<String>,

    /**
     * The local images. Some of these may have been uploaded to backend already. The images
     * should be in the same order they were added i.e. the last image added should be the last
     * in the list.
     */
    val localImages: List<EntityImage>,
) {
    val primaryImage: EntityImage? by lazy {
        // Figure out the primary image based on requirements presented in the constructor.
        // - the last images are the latest (remote + local)
        // - there may be a "local only" image that user has added. We should prefer those over
        //   uploaded images

        // only local image is something that has not yet been uploaded and
        // thus its id does not exist in remoteImageIds
        val lastOnlyLocalImage = localImages
            .lastOrNull { remoteImageIds.contains(it.serverId).not() }

        lastOnlyLocalImage
            ?: remoteImageIds.lastOrNull()?.let { remoteImageId ->
                // there may be a local image matching this one
                localImages.firstOrNull { it.serverId == remoteImageId }
                    ?: EntityImage(
                        serverId = remoteImageId,
                        localIdentifier = null,
                        localUrl = null,
                        status = EntityImage.Status.UPLOADED
                    )
            }
    }

    fun withNewPrimaryImage(image: EntityImage): EntityImages {
        // update current local images in order to mark those that need to be removed
        val existingLocalImages = localImages.map { localImage ->
            when (localImage.status) {
                EntityImage.Status.LOCAL ->
                    // previously added image, but not yet uploaded
                    // -> mark to be removed
                    localImage.copy(status = EntityImage.Status.LOCAL_TO_BE_REMOVED)
                EntityImage.Status.LOCAL_TO_BE_REMOVED,
                EntityImage.Status.UPLOADED ->
                    // image either already marked for deletion or it has been uploaded
                    // i.e. should not require further actions
                    localImage
            }
        }

        return copy(
            // last one is the latest / primary image, see primaryImage
            localImages = existingLocalImages + image.copy(
                status = EntityImage.Status.LOCAL
            )
        )
    }

    fun withLocalImageUploaded(serverId: String): EntityImages {
        val newLocalImages = localImages.map { localImage ->
            when (localImage.serverId) {
                serverId ->
                    // Change status to uploaded
                    localImage.copy(status = EntityImage.Status.UPLOADED)
                else ->
                    localImage
            }
        }

        val newRemoteImageIds = if (!remoteImageIds.contains(serverId)) {
            remoteImageIds + serverId
        } else {
            remoteImageIds
        }

        return copy(
            localImages = newLocalImages,
            remoteImageIds = newRemoteImageIds,
        )
    }

    fun withLocalImagesRemoved(removedServerIds: List<String>): EntityImages {
        return copy(
            localImages = localImages.filter { localImage -> removedServerIds.contains(localImage.serverId).not() },
            remoteImageIds = remoteImageIds,
        )
    }

    fun withDeletedImagesRemoved(): EntityImages {
        return copy(
            localImages = localImages.filter { it.status != EntityImage.Status.LOCAL_TO_BE_REMOVED },
            remoteImageIds = remoteImageIds,
        )
    }

    companion object {
        internal fun noImages() = EntityImages(listOf(), listOf())
    }
}

fun EntityImages.isEmpty(): Boolean {
    return localImages.isEmpty() && remoteImageIds.isEmpty()
}
