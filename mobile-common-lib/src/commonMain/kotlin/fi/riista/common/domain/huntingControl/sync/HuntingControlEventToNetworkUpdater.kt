package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.toHuntingControlEventData
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.toHuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.toHuntingControlEventDTO
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.calls.NetworkResponse

internal class HuntingControlEventToNetworkUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    val commonFileProvider: CommonFileProvider,
) {
    private val repository = HuntingControlRepository(database)

    suspend fun update(events: List<HuntingControlEvent>): Boolean {
        var success = true
        events.forEach { event ->
            if (event.remoteId == null) {
                // new event
                val createDTO = event.toHuntingControlEventCreateDTO()
                success = if (createDTO != null) {
                    val response = backendApiProvider.backendAPI.createHuntingControlEvent(event.rhyId, createDTO)
                    success && handleNetworkResponse(
                        response = response,
                        eventFromDb = event,
                        localId = event.localId,
                        rhyId = event.rhyId,
                    )
                } else {
                    logger.w { "Unable to create event with localId ${event.localId}" }
                    false
                }
            } else {
                val updateDTO = event.toHuntingControlEventDTO()
                success = if (updateDTO != null) {
                    val response = backendApiProvider.backendAPI.updateHuntingControlEvent(event.rhyId, updateDTO)
                    deleteAttachments(event.attachments)
                    success && handleNetworkResponse(
                        response = response,
                        eventFromDb = event,
                        localId = event.localId,
                        rhyId = event.rhyId,
                    )
                } else {
                    logger.w { "Unable to update event with localId ${event.localId}" }
                    false
                }
            }
        }
        return success
    }

    private suspend fun handleNetworkResponse(
        response: NetworkResponse<HuntingControlEventDTO>,
        eventFromDb:  HuntingControlEvent,
        localId: Long,
        rhyId: OrganizationId
    ): Boolean {
        response.onSuccess { _, data ->
            val sentEvent = data.typed.toHuntingControlEventData(
                localId = localId,
                rhyId = rhyId,
                modified = false,
            )
            if (sentEvent?.remoteId != null) {
                repository.updateHuntingControlEvent(sentEvent)
                uploadAttachments(sentEvent.remoteId, eventFromDb.attachments)
            }
        }
        response.onError { statusCode, exception ->
            logger.w { "Error while syncing hunting control event to backend: $statusCode, ${exception?.message}" }
            return false
        }
        return true
    }

    private suspend fun deleteAttachments(attachments: List<HuntingControlAttachment>) {
        attachments.forEach { attachment ->
            if (attachment.deleted && attachment.remoteId != null && attachment.localId != null) {
                val response = backendApiProvider.backendAPI.deleteHuntingControlEventAttachment(attachmentId = attachment.remoteId)
                response.onSuccessWithoutData {
                    repository.deleteAttachment(attachment.localId)
                }
            }
        }
    }

    private suspend fun uploadAttachments(eventRemoteId: Long, attachments: List<HuntingControlAttachment>) {
        attachments.filter { attachment -> attachment.remoteId == null }
            .forEach { attachment ->
                val uuid = attachment.uuid
                val contentType = attachment.mimeType
                if (uuid != null) {
                    val file = commonFileProvider.getFile(
                        directory = CommonFileProvider.Directory.ATTACHMENTS,
                        fileUuid = uuid,
                    )

                    if (contentType == null || file == null || !file.exists()) {
                        logger.w { "Unable to upload attachment (localId=${attachment.localId} uuid=$uuid, contentType=$contentType, exists=${file?.exists()}" }
                    } else {
                        val response = backendApiProvider.backendAPI.uploadHuntingControlEventAttachment(
                            eventRemoteId = eventRemoteId,
                            uuid = uuid,
                            fileName = attachment.fileName,
                            contentType = contentType,
                            file = file,
                        )
                        response.onSuccess { _, data ->
                            // Attachment was successfully sent to backend, update remoteId to DB
                            attachment.localId?.let {
                                repository.updateAttachmentRemoteId(remoteId = data.typed, localId = attachment.localId)
                            }
                        }
                    }
                } else {
                    logger.w { "Unable to upload attachment because uuid is null" }
                }
            }
    }

    companion object {
        private val logger by getLogger(HuntingControlEventToNetworkUpdater::class)
    }
}
