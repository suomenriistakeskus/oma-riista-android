package fi.riista.common.groupHunting.network

import fi.riista.common.groupHunting.dto.HuntingGroupMembersDTO
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHuntingGroupMembers(
    private val huntingGroupId: HuntingGroupId,
): NetworkRequest<HuntingGroupMembersDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingGroupMembersDTO> {
        return client.request(
                request = {
                    get(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/$huntingGroupId/members") {
                        accept(ContentType.Application.Json)
                    }
                },
                configureResponseHandler = {
                    // nop, default response handling works just fine
                }
        )
    }
}