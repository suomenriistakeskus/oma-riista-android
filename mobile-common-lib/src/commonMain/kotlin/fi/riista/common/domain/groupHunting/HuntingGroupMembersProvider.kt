package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.dto.HuntingGroupMembersDTO
import fi.riista.common.domain.groupHunting.dto.toMember
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface HuntingGroupMembersProvider: DataFetcher {
    /**
     * The members of the hunting group.
     *
     * The value should only be null if members have not been loaded successfully at any point.
     * This means that if members have been successfully loaded but subsequent load requests
     * fail, the list may still contain members from previous load attempt.
     */
    val members: List<HuntingGroupMember>?
}

/**
 * Provides hunting group members by fetching them from backend.
 */
internal class HuntingGroupMembersFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : HuntingGroupMembersProvider,
    NetworkDataFetcher<HuntingGroupMembersDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _members = mutableListOf<HuntingGroupMember>()
    override val members: List<HuntingGroupMember>?
        get() {
            // _members may be empty because of two reasons:
            // 1. there are no members
            // 2. the loading failed
            //
            // we should return null only in the latter case
            return if (_members.isEmpty() && !loadStatus.value.loaded) {
                null
            } else {
                _members
            }
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<HuntingGroupMembersDTO> =
        backendAPI.fetchHuntingGroupMembers(huntingGroupId)

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out HuntingGroupMembersDTO>
    ) {
        val newMembers = responseData.typed
            .map { memberDTO ->
                memberDTO.toMember()
            }
            .takeIf { it.isNotEmpty() }

        _members.clear()
        _members.addAll(newMembers ?: listOf())
    }

    override fun handleError401() {
        _members.clear()
    }
}
