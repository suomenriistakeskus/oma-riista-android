package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider

internal class DeletedObservationsUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = ObservationRepository(database)

    suspend fun updateToBackend(username: String) {
        val events = repository.getDeletedObservations(username)
        events.forEach { observation ->
            updateToBackend(observation = observation)
        }
    }

    internal suspend fun updateToBackend(observation: CommonObservation) {
        val backendAPI = backendApiProvider.backendAPI

        when (observation.remoteId) {
            null -> {
                // Not sent to backend, just delete from database
                repository.hardDelete(observation)
            }
            else -> {
                val response = backendAPI.deleteObservation(observation.remoteId)
                response.onSuccessWithoutData {
                    repository.hardDelete(observation)
                }
                response.onError { statusCode, _ ->
                    logger.w { "Failed to delete observation from server. Status code $statusCode" }
                }

                response.onCancel {
                    logger.w { "Failed to delete observation from server (cancelled?)" }
                }
            }
        }
    }

    suspend fun fetchFromBackend(username: String, deletedAfter: LocalDateTime?): LocalDateTime? {
        val backendAPI = backendApiProvider.backendAPI
        val response = backendAPI.fetchDeletedObservations(deletedAfter)
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
        private val logger by getLogger(DeletedObservationsUpdater::class)
    }
}
