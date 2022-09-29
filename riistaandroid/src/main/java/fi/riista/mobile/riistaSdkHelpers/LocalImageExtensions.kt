package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.EntityImage
import fi.riista.mobile.models.LocalImage

fun LocalImage.toLocalEntityImage(uploaded: Boolean) = EntityImage(
    serverId = serverId,
    localIdentifier = null,
    localUrl = localPath,
    status = if (uploaded) {
        EntityImage.Status.UPLOADED
    } else {
        EntityImage.Status.LOCAL
    },
)
