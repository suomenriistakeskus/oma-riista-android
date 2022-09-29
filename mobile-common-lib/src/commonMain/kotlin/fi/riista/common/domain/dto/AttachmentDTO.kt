package fi.riista.common.domain.dto

import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import kotlinx.serialization.Serializable

@Serializable
data class AttachmentDTO(
    val id: Long,
    val fileName: String,
    val isImage: Boolean,
    val thumbnail: String?, // Image thumbnail as base64 encoded string
)

fun AttachmentDTO.toHuntingControlAttachment(): HuntingControlAttachment {
    return HuntingControlAttachment(
        remoteId = id,
        localId = null,
        fileName = fileName,
        isImage = isImage,
        thumbnailBase64 = thumbnail,
    )
}
