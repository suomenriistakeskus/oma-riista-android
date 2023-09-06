package fi.riista.common.domain.harvest.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.toCommonHarvest
import fi.riista.common.domain.harvest.sync.dto.toHarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.toHarvestDTO
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.calls.NetworkResponse

internal class HarvestToNetworkUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = HarvestRepository(database)

    /**
     * Sends the given harvest [harvests] to backend and returns a list containing the updated
     * [CommonHarvest]s (most likely [CommonHarvest.rev] will differ).
     */
    suspend fun update(username: String, harvests: List<CommonHarvest>): List<CommonHarvest> {
        val updatedHarvests: List<CommonHarvest> = harvests.map { harvest ->
            when (val updateResponse = sendHarvestToBackend(username, harvest)) {
                is HarvestOperationResponse.Error,
                is HarvestOperationResponse.SaveFailure,
                is HarvestOperationResponse.NetworkFailure -> harvest
                is HarvestOperationResponse.Success -> updateResponse.harvest
            }
        }

        return updatedHarvests
    }

    /**
     * Sends the given harvest [harvest] to backend and returns a list containing the updated
     * [CommonHarvest]s (most likely [CommonHarvest.rev] will differ).
     */
    suspend fun sendHarvestToBackend(username: String, harvest: CommonHarvest): HarvestOperationResponse {
        if (harvest.localId == null) {
            return HarvestOperationResponse.NetworkFailure(
                statusCode = null, errorMessage = "missing local id"
            )
        }

        val backendAPI = backendApiProvider.backendAPI

        val networkResponse: NetworkResponse<HarvestDTO> = if (harvest.id == null) {
            val createDTO = harvest.toHarvestCreateDTO()
            backendAPI.createHarvest(createDTO)
        } else {
            val updateDTO = harvest.toHarvestDTO() ?: kotlin.run {
                return HarvestOperationResponse.Error(
                    errorMessage = "couldn't create update-dto for harvest ${harvest.id} / ${harvest.localId}"
                )
            }

            backendAPI.updateHarvest(updateDTO)
        }

        networkResponse.onSuccess { _, data ->
            val responseHarvest = data.typed.toCommonHarvest(localId = harvest.localId, modified = false, deleted = false)
            return if (responseHarvest != null) {
                // Keep local images from local copy, as they are not sent here
                try {
                    HarvestOperationResponse.Success(
                        harvest = repository.upsertHarvest(
                            username = username,
                            harvest = responseHarvest.copy(
                                images = EntityImages(
                                    remoteImageIds = responseHarvest.images.remoteImageIds,
                                    localImages = harvest.images.localImages,
                                )
                            )
                        )
                    )
                } catch (e: Exception) {
                    RiistaSDK.crashlyticsLogger.log(e, "Unable to save harvest to DB. id=${responseHarvest.id}")
                    HarvestOperationResponse.SaveFailure(e.message)
                }
            } else {
                HarvestOperationResponse.NetworkFailure(
                    statusCode = null,
                    errorMessage = "Failed to convert successful response to harvest (id = ${data.typed.id})"
                )
            }
        }

        networkResponse.onError { statusCode, exception ->
            return HarvestOperationResponse.NetworkFailure(statusCode = statusCode, errorMessage = exception?.message)
        }

        return HarvestOperationResponse.NetworkFailure(
            statusCode = null,
            errorMessage = "not success nor error (cancelled?)"
        )
    }
}
