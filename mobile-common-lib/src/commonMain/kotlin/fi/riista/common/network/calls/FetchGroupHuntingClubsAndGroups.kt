package fi.riista.common.network.calls

import fi.riista.common.groupHunting.dto.GroupHuntingClubsAndGroupsDTO
import fi.riista.common.network.NetworkClient
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchGroupHuntingClubsAndGroups: NetworkRequest<GroupHuntingClubsAndGroupsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingClubsAndGroupsDTO> {
        return client.request(
                request = {
                    get(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/groups") {
                        accept(ContentType.Application.Json)
                    }
                },
                configureResponseHandler = {
                    // nop, default response handling works just fine
                }
        )
    }
}