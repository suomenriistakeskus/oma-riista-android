package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.dto.RejectDiaryEntryDTO
import fi.riista.common.domain.groupHunting.dto.toGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider


sealed class GroupHuntingHarvestOperationResponse {
    /**
     * The operation succeeded.
     */
    data class Success(val harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse()

    /**
     * The operation failed.
     */
    data class Failure(val networkStatusCode: Int?): GroupHuntingHarvestOperationResponse()

    /**
     * The harvest data was invalid or some other precondition failed.
     */
    object Error: GroupHuntingHarvestOperationResponse()
}

interface GroupHuntingHarvestUpdater {
    suspend fun createHarvest(harvest: GroupHuntingHarvestData): GroupHuntingHarvestOperationResponse
    suspend fun updateHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse
    suspend fun rejectHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse
}

internal class GroupHuntingHarvestNetworkUpdater(
    private val backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : GroupHuntingHarvestUpdater {

    override suspend fun createHarvest(harvest: GroupHuntingHarvestData): GroupHuntingHarvestOperationResponse {
        val harvestDTO = harvest.toGroupHuntingHarvestCreateDTO() ?: kotlin.run {
            logger.w { "Failed to create 'createDTO' based on harvest data" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        if (harvestDTO.huntingDayId == null) {
            logger.w { "Refusing to create harvest. No hunting day" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        val response = backendApiProvider.backendAPI.createGroupHuntingHarvest(harvestDTO)

        val createdHarvest = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toGroupHuntingHarvest()
        }

        if (createdHarvest != null) {
            return GroupHuntingHarvestOperationResponse.Success(createdHarvest)
        }

        return GroupHuntingHarvestOperationResponse.Failure(response.statusCode)
    }

    override suspend fun updateHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse {
        val harvestDTO = harvest.toGroupHuntingHarvestDTO()

        if (harvestDTO.huntingDayId == null) {
            logger.w { "Refusing to update harvest. No hunting day" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        val response = backendApiProvider.backendAPI.updateGroupHuntingHarvest(harvestDTO)

        val updatedHarvest = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toGroupHuntingHarvest()
        }

        if (updatedHarvest != null) {
            return GroupHuntingHarvestOperationResponse.Success(updatedHarvest)
        }

        return GroupHuntingHarvestOperationResponse.Failure(response.statusCode)
    }

    override suspend fun rejectHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse  {
        val rejectDiaryEntryDTO = RejectDiaryEntryDTO(
            type = DiaryEntryType.HARVEST.rawBackendEnumValue,
            id = huntingGroupId,
            entryId = harvest.id
        )

        val response = backendApiProvider.backendAPI.rejectGroupHuntingDiaryEntry(rejectDiaryEntryDTO)
        response.onSuccessWithoutData { return GroupHuntingHarvestOperationResponse.Success(harvest) }
        return GroupHuntingHarvestOperationResponse.Failure(response.statusCode)
    }

    companion object {
        private val logger by getLogger(GroupHuntingHarvestNetworkUpdater::class)
    }
}
