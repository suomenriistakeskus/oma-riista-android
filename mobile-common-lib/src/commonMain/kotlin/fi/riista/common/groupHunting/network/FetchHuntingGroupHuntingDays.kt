package fi.riista.common.groupHunting.network

import fi.riista.common.groupHunting.dto.GroupHuntingDaysDTO
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHuntingGroupHuntingDays(
    private val huntingGroupId: HuntingGroupId,
): NetworkRequest<GroupHuntingDaysDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingDaysDTO> {
        return client.request(
                request = {
                    get(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/$huntingGroupId/huntingdays") {
                        accept(ContentType.Application.Json)
                    }
                },
                configureResponseHandler = {
                    // nop, default response handling works just fine
                }
        )
    }
}