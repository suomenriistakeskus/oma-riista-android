package fi.riista.common.domain.huntingControl.model

import fi.riista.common.domain.dto.AttachmentDTO
import io.matthewnelson.component.base64.encodeBase64
import kotlinx.serialization.Serializable

@Serializable
data class HuntingControlAttachment(
    val localId: Long? = null,
    val remoteId: Long? = null,
    val fileName: String,
    val isImage: Boolean = false,
    val thumbnailBase64: String? = null,
    val deleted: Boolean = false,
    val uuid: String? = null,
    val mimeType: String? = null,
)

fun HuntingControlAttachment.toAttachmentDTO(): AttachmentDTO? {
    if (remoteId == null) {
        return null
    }
    return AttachmentDTO(
        id = remoteId,
        fileName = fileName,
        isImage = isImage,
        thumbnail = thumbnailBase64,
    )
}

fun ByteArray.encodeToBase64(): String? {
    return this.encodeBase64()
}
