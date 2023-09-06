package fi.riista.common.domain.huntingclub.memberships.sync

import fi.riista.common.domain.huntingclub.memberships.dto.HuntingClubMembershipsDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHuntingClubMemberships: NetworkRequest<HuntingClubMembershipsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingClubMembershipsDTO> {
        return client.request(
            request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/club/my-memberships") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
