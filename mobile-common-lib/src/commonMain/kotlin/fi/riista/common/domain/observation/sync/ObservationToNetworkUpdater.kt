package fi.riista.common.domain.observation.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.observation.ObservationOperationResponse
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.toCommonObservation
import fi.riista.common.domain.observation.sync.dto.toObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.toObservationDTO
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.calls.NetworkResponse

internal class ObservationToNetworkUpdater(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
) {
    private val repository = ObservationRepository(database)

    /**
     * Sends the given observation [observations] to backend and returns a list containing the updated
     * [CommonObservation]s (most likely [CommonObservation.revision] will differ).
     */
    suspend fun update(username: String, observations: List<CommonObservation>): List<CommonObservation> {
        val updatedObservations: List<CommonObservation> = observations.map { observation ->
            when (val updateResponse = sendObservationToBackend(username, observation)) {
                is ObservationOperationResponse.Error,
                is ObservationOperationResponse.SaveFailure,
                is ObservationOperationResponse.NetworkFailure -> observation
                is ObservationOperationResponse.Success -> updateResponse.observation
            }
        }

        return updatedObservations
    }

    /**
     * Sends the given observation [observation] to backend and returns a list containing the updated
     * [CommonObservation]s (most likely [CommonObservation.revision] will differ).
     */
    suspend fun sendObservationToBackend(username: String, observation: CommonObservation): ObservationOperationResponse {
        if (observation.localId == null) {
            return ObservationOperationResponse.NetworkFailure(
                statusCode = null, errorMessage = "missing local id"
            )
        }

        val backendAPI = backendApiProvider.backendAPI

        val networkResponse: NetworkResponse<ObservationDTO> = if (observation.remoteId == null) {
            val createDTO = observation.toObservationCreateDTO() ?: kotlin.run {
                return ObservationOperationResponse.Error(
                    errorMessage = "couldn't create create-dto for observation ${observation.localId}"
                )
            }

            backendAPI.createObservation(createDTO)
        } else {
            val updateDTO = observation.toObservationDTO() ?: kotlin.run {
                return ObservationOperationResponse.Error(
                    errorMessage = "couldn't create update-dto for observation ${observation.remoteId} / ${observation.localId}"
                )
            }

            backendAPI.updateObservation(updateDTO)
        }

        networkResponse.onSuccess { _, data ->
            val responseObservation = data.typed.toCommonObservation(localId = observation.localId, modified = false, deleted = false)
            return if (responseObservation != null) {
                // Keep local images from local copy, as they are not sent here
                try {
                    ObservationOperationResponse.Success(
                        observation = repository.upsertObservation(
                            username = username,
                            observation = responseObservation.copy(
                                images = EntityImages(
                                    remoteImageIds = responseObservation.images.remoteImageIds,
                                    localImages = observation.images.localImages,
                                )
                            )
                        )
                    )
                } catch (e: Exception) {
                    RiistaSDK.crashlyticsLogger.log(e, "Unable to save observation to DB. remoteId=${responseObservation.remoteId}")
                    ObservationOperationResponse.SaveFailure(e.message)
                }
            } else {
                ObservationOperationResponse.NetworkFailure(
                    statusCode = null,
                    errorMessage = "Failed to convert successful response to observation (remoteId = ${data.typed.id})"
                )
            }
        }

        networkResponse.onError { statusCode, exception ->
            return ObservationOperationResponse.NetworkFailure(statusCode = statusCode, errorMessage = exception?.message)
        }

        return ObservationOperationResponse.NetworkFailure(
            statusCode = null,
            errorMessage = "not success nor error (cancelled?)"
        )
    }
}
