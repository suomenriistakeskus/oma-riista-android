package fi.riista.common.huntingclub.network

import fi.riista.common.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

class FetchHuntingClubMemberInvitations: NetworkRequest<HuntingClubMemberInvitationsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingClubMemberInvitationsDTO> {
        return client.request(
            request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/club/invitations") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
