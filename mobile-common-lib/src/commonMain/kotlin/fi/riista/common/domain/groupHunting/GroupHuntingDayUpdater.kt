package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayForDeerDTO
import fi.riista.common.domain.groupHunting.dto.toHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.IdentifiesHuntingGroup
import fi.riista.common.domain.groupHunting.model.toHuntingDayCreateDTO
import fi.riista.common.domain.groupHunting.model.toHuntingDayUpdateDTO
import fi.riista.common.model.LocalDate
import fi.riista.common.model.toLocalDateDTO
import fi.riista.common.network.BackendApiProvider

sealed class GroupHuntingDayUpdateResponse {
    /**
     * Hunting day was updated on the backend.
     */
    data class Updated(val huntingDay: GroupHuntingDay): GroupHuntingDayUpdateResponse()

    /**
     * Updating failed on the backend.
     */
    data class Failed(val networkStatusCode: Int?): GroupHuntingDayUpdateResponse()

    /**
     * The hunting day data was invalid or some other precondition failed.
     */
    object Error: GroupHuntingDayUpdateResponse()
}

sealed class GroupHuntingDayForDeerResponse {
    /**
     * Backend returned a group hunting day.
     */
    data class Success(val huntingDay: GroupHuntingDay): GroupHuntingDayForDeerResponse()

    /**
     * Fetching group hunting day failed.
     */
    data class Failed(val networkStatusCode: Int?): GroupHuntingDayForDeerResponse()
}


interface GroupHuntingDayUpdater {
    /**
     * Creates a new hunting day e.g. on backend.
     */
    suspend fun createHuntingDay(huntingDay: GroupHuntingDay): GroupHuntingDayUpdateResponse

    /**
     * Updates the hunting day e.g. on backend.
     */
    suspend fun updateHuntingDay(huntingDay: GroupHuntingDay): GroupHuntingDayUpdateResponse

    /**
     * Fetch hunting day for deer. Backend either provides an existing hunting day or creates a new one.
     */
    suspend fun fetchHuntingDayForDeer(identifiesHuntingGroup: IdentifiesHuntingGroup, date: LocalDate)
            : GroupHuntingDayForDeerResponse
}

/**
 * A [GroupHuntingDayUpdater] that is able to create and update hunting day data on the backend.
 */
internal class GroupHuntingDayNetworkUpdater(
    private val backendApiProvider: BackendApiProvider,
) : GroupHuntingDayUpdater {

    override suspend fun createHuntingDay(huntingDay: GroupHuntingDay): GroupHuntingDayUpdateResponse {
        val huntingDayDTO = huntingDay.toHuntingDayCreateDTO()

        val response = backendApiProvider.backendAPI.createHuntingGroupHuntingDay(huntingDayDTO)

        val updatedHuntingDay = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toHuntingDay()
        }

        if (updatedHuntingDay != null) {
            return GroupHuntingDayUpdateResponse.Updated(updatedHuntingDay)
        }

        return GroupHuntingDayUpdateResponse.Failed(response.statusCode)
    }

    override suspend fun updateHuntingDay(huntingDay: GroupHuntingDay): GroupHuntingDayUpdateResponse {
        val huntingDayDTO = huntingDay.toHuntingDayUpdateDTO()
                ?: return GroupHuntingDayUpdateResponse.Error

        val response = backendApiProvider.backendAPI.updateHuntingGroupHuntingDay(huntingDayDTO)

        val updatedHuntingDay = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toHuntingDay()
        }

        if (updatedHuntingDay != null) {
            return GroupHuntingDayUpdateResponse.Updated(updatedHuntingDay)
        }

        return GroupHuntingDayUpdateResponse.Failed(response.statusCode)
    }

    override suspend fun fetchHuntingDayForDeer(
        identifiesHuntingGroup: IdentifiesHuntingGroup,
        date: LocalDate
    ): GroupHuntingDayForDeerResponse {
        val request = GroupHuntingDayForDeerDTO(
            huntingGroupId = identifiesHuntingGroup.huntingGroupId,
            date = date.toLocalDateDTO(),
        )
        val response = backendApiProvider.backendAPI.fetchHuntingGroupHuntingDayForDeer(request)

        val huntingDay = response.transformSuccessData { _, responseDTO ->
            responseDTO.typed.toHuntingDay()
        }

        if (huntingDay != null) {
            return GroupHuntingDayForDeerResponse.Success(huntingDay)
        }

        return GroupHuntingDayForDeerResponse.Failed(response.statusCode)
    }
}
