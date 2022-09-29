package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.dto.RejectDiaryEntryDTO
import fi.riista.common.domain.groupHunting.dto.toGroupHuntingObservation
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.network.BackendApiProvider

sealed class GroupHuntingObservationOperationResponse {
    /**
     * The operation succeeded.
     */
    data class Success(val observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse()

    /**
     * The operation failed.
     */
    data class Failure(val networkStatusCode: Int?): GroupHuntingObservationOperationResponse()

    /**
     * The observation data was invalid or some other precondition failed.
     */
    object Error: GroupHuntingObservationOperationResponse()
}

interface GroupHuntingObservationUpdater {
    suspend fun createObservation(observation: GroupHuntingObservationData): GroupHuntingObservationOperationResponse
    suspend fun acceptObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse
    suspend fun rejectObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse
}

internal class GroupHuntingObservationNetworkUpdater(
    private val backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : GroupHuntingObservationUpdater {

    override suspend fun createObservation(observation: GroupHuntingObservationData): GroupHuntingObservationOperationResponse {
        val observationDTO = observation.toGroupHuntingObservationCreateDTO()

        if (observationDTO.huntingDayId == null) {
            return GroupHuntingObservationOperationResponse.Error
        }

        val response = backendApiProvider.backendAPI.createGroupHuntingObservation(observationDTO)

        val createdObservation = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toGroupHuntingObservation()
        }

        if (createdObservation != null) {
            return GroupHuntingObservationOperationResponse.Success(createdObservation)
        }

        return GroupHuntingObservationOperationResponse.Failure(response.statusCode)
    }

    override suspend fun acceptObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse {
        val observationDTO = observation.toGroupHuntingObservationUpdateDTO()

        if (observationDTO.huntingDayId == null) {
            return GroupHuntingObservationOperationResponse.Error
        }

        val response = backendApiProvider.backendAPI.updateGroupHuntingObservation(observationDTO)

        val updatedObservation = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toGroupHuntingObservation()
        }

        if (updatedObservation != null) {
            return GroupHuntingObservationOperationResponse.Success(updatedObservation)
        }

        return GroupHuntingObservationOperationResponse.Failure(response.statusCode)
    }

    override suspend fun rejectObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse {
        val rejectDiaryEntryDTO = RejectDiaryEntryDTO(
            type = DiaryEntryType.OBSERVATION.rawBackendEnumValue,
            id = huntingGroupId,
            entryId = observation.id
        )

        val response = backendApiProvider.backendAPI.rejectGroupHuntingDiaryEntry(rejectDiaryEntryDTO)
        response.onSuccessWithoutData { return GroupHuntingObservationOperationResponse.Success(observation) }
        return GroupHuntingObservationOperationResponse.Failure(response.statusCode)
    }
}
