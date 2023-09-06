package fi.riista.common.database.util

import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages

internal object DbImageUtil {
    fun updateLocalImagesFromRemote(
        existingImages: EntityImages,
        updatingImages: EntityImages,
    ): List<EntityImage> {
        // Check if any local image has been removed on remote. If so then remove it also from local images.
        val removedImages = existingImages.localImages
            .filter { image -> image.status == EntityImage.Status.UPLOADED }
            .filter { image -> updatingImages.remoteImageIds.contains(image.serverId).not() }
            .mapNotNull { image -> image.serverId }
        return existingImages.withLocalImagesRemoved(removedImages).localImages
    }

}
