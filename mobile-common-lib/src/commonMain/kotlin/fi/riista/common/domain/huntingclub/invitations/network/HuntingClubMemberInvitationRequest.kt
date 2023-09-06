package fi.riista.common.domain.huntingclub.invitations.network

import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitationId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.put

internal sealed class HuntingClubMemberInvitationRequest(
    private val invitationId: HuntingClubMemberInvitationId,
    private val requestType: RequestType,
) : NetworkRequest<Unit> {

    enum class RequestType(val type: String) {
        ACCEPT("accept"),
        REJECT("reject")
    }

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {

        return client.request(
            request = {
                put(urlString = "${client.serverBaseAddress}/api/mobile/v2/club/invitation/$invitationId/${requestType.type}") {
                }
            },
            configureResponseHandler = {
                onSuccess { httpResponse ->
                    NetworkResponse.SuccessWithNoData(httpResponse.status.value)
                }
            }
        )
    }

    internal class Accept(invitationId: HuntingClubMemberInvitationId) : HuntingClubMemberInvitationRequest(
        invitationId = invitationId,
        requestType = RequestType.ACCEPT
    )

    internal class Reject(invitationId: HuntingClubMemberInvitationId) : HuntingClubMemberInvitationRequest(
        invitationId = invitationId,
        requestType = RequestType.REJECT
    )
}
