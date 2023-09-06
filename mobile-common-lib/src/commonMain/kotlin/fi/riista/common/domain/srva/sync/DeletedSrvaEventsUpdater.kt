package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider

internal class DeletedSrvaEventsUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = SrvaEventRepository(database)

    suspend fun updateToBackend(username: String) {
        val events = repository.getDeletedEvents(username)
        events.forEach { srvaEvent ->
            updateToBackend(srvaEvent = srvaEvent)
        }
    }

    suspend fun updateToBackend(srvaEvent: CommonSrvaEvent) {
        val backendAPI = backendApiProvider.backendAPI

        when (srvaEvent.remoteId) {
            null -> {
                // Not sent to backend, just delete from database
                repository.hardDelete(srvaEvent)
            }
            else -> {
                val response = backendAPI.deleteSrvaEvent(srvaEvent.remoteId)
                response.onSuccessWithoutData {
                    repository.hardDelete(srvaEvent)
                }
                response.onError { statusCode, _ ->
                    logger.w { "Failed to delete SRVA event from server. Status code $statusCode" }
                }
            }
        }
    }

    suspend fun fetchFromBackend(username: String, deletedAfter: LocalDateTime?): LocalDateTime? {
        val backendAPI = backendApiProvider.backendAPI
        val response = backendAPI.fetchDeletedSrvaEvents(deletedAfter)
        response.onSuccess { _, data ->
            data.typed.entryIds.forEach { remoteId ->
                repository.hardDeleteByRemoteId(username, remoteId)
            }
            return data.typed.latestEntry?.toLocalDateTime()
        }
        response.onError { statusCode, _ ->
            logger.w { "Failed to fetch deleted events from server. Status code $statusCode" }
        }
        return null
    }

    companion object {
        private val logger by getLogger(DeletedSrvaEventsUpdater::class)
    }
}
