package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider

internal class DeletedHarvestsUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = HarvestRepository(database)

    suspend fun updateToBackend(username: String) {
        val events = repository.getDeletedHarvests(username)
        events.forEach { harvest ->
            updateToBackend(harvest = harvest)
        }
    }

    internal suspend fun updateToBackend(harvest: CommonHarvest) {
        val backendAPI = backendApiProvider.backendAPI

        when (harvest.id) {
            null -> {
                // Not sent to backend, just delete from database
                repository.hardDelete(harvest)
            }
            else -> {
                val response = backendAPI.deleteHarvest(harvest.id)
                response.onSuccessWithoutData {
                    repository.hardDelete(harvest)
                }
                response.onError { statusCode, _ ->
                    logger.w { "Failed to delete harvest from server. Status code $statusCode" }
                }

                response.onCancel {
                    logger.w { "Failed to delete harvest from server (cancelled?)" }
                }
            }
        }
    }

    suspend fun fetchFromBackend(username: String, deletedAfter: LocalDateTime?): LocalDateTime? {
        val backendAPI = backendApiProvider.backendAPI
        val response = backendAPI.fetchDeletedHarvests(deletedAfter)
        response.onSuccess { _, data ->
            data.typed.entryIds.forEach { remoteId ->
                repository.hardDeleteByRemoteId(username, remoteId)
            }
            return data.typed.latestEntry?.toLocalDateTime()
        }
        response.onError { statusCode, _ ->
            logger.w { "Failed to fetch deleted harvests from server. Status code $statusCode" }
        }
        return null
    }

    companion object {
        private val logger by getLogger(DeletedHarvestsUpdater::class)
    }
}
