package fi.riista.common.domain.srva.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.srva.SrvaEventOperationResponse
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.toCommonSrvaEvent
import fi.riista.common.domain.srva.sync.dto.toSrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.toSrvaEventDTO
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.calls.NetworkResponse

internal class SrvaEventToNetworkUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = SrvaEventRepository(database)

    /**
     * Sends the given srva [events] to backend and returns a list containing the updated
     * [CommonSrvaEvent]s (most likely [CommonSrvaEvent.revision] will differ).
     */
    suspend fun update(username: String, events: List<CommonSrvaEvent>): List<CommonSrvaEvent> {
        val updatedEvents: List<CommonSrvaEvent> = events.map { srvaEvent ->
            when (val updateResponse = sendSrvaEventToBackend(username, srvaEvent)) {
                is SrvaEventOperationResponse.Error,
                is SrvaEventOperationResponse.SaveFailure,
                is SrvaEventOperationResponse.NetworkFailure -> srvaEvent
                is SrvaEventOperationResponse.Success -> updateResponse.srvaEvent
            }
        }

        return updatedEvents
    }

    /**
     * Sends the given srva [srvaEvent] to backend and returns a list containing the updated
     * [CommonSrvaEvent]s (most likely [CommonSrvaEvent.revision] will differ).
     */
    suspend fun sendSrvaEventToBackend(username: String, srvaEvent: CommonSrvaEvent): SrvaEventOperationResponse {
        if (srvaEvent.localId == null) {
            return SrvaEventOperationResponse.NetworkFailure(
                statusCode = null, errorMessage = "missing local id"
            )
        }

        val backendAPI = backendApiProvider.backendAPI

        val networkResponse: NetworkResponse<SrvaEventDTO> = if (srvaEvent.remoteId == null) {
            val createDTO = srvaEvent.toSrvaEventCreateDTO() ?: kotlin.run {
                return SrvaEventOperationResponse.Error(
                    errorMessage = "couldn't create create-dto for srva ${srvaEvent.localId}"
                )
            }

            backendAPI.createSrvaEvent(createDTO)
        } else {
            val updateDTO = srvaEvent.toSrvaEventDTO() ?: kotlin.run {
                return SrvaEventOperationResponse.Error(
                    errorMessage = "couldn't create update-dto for srva ${srvaEvent.remoteId} / ${srvaEvent.localId}"
                )
            }

            backendAPI.updateSrvaEvent(updateDTO)
        }

        networkResponse.onSuccess { _, data ->
            val responseEvent = data.typed.toCommonSrvaEvent(localId = srvaEvent.localId, modified = false, deleted = false)
            return if (responseEvent != null) {
                // Keep local images from local copy, as they are not sent here
                try {
                    SrvaEventOperationResponse.Success(
                        srvaEvent = repository.upsertSrvaEvent(
                            username = username,
                            srvaEvent = responseEvent.copy(
                                images = EntityImages(
                                    remoteImageIds = responseEvent.images.remoteImageIds,
                                    localImages = srvaEvent.images.localImages,
                                )
                            )
                        )
                    )
                } catch (e: Exception) {
                    RiistaSDK.crashlyticsLogger.log(e, "Unable to save SRVA to DB. remoteId=${responseEvent.remoteId}")
                    SrvaEventOperationResponse.SaveFailure(e.message)
                }
            } else {
                SrvaEventOperationResponse.NetworkFailure(
                    statusCode = null,
                    errorMessage = "Failed to convert successful response to srva event (remoteId = ${data.typed.id})"
                )
            }
        }

        networkResponse.onError { statusCode, exception ->
            return SrvaEventOperationResponse.NetworkFailure(statusCode = statusCode, errorMessage = exception?.message)
        }

        return SrvaEventOperationResponse.NetworkFailure(
            statusCode = null,
            errorMessage = "not success nor error (cancelled?)"
        )
    }
}
