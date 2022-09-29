package fi.riista.common.domain.huntingclub

import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.network.BackendApiProvider

sealed class HuntingClubMemberInvitationOperationResponse {

    object Success: HuntingClubMemberInvitationOperationResponse()
    data class Failure(val networkStatusCode: Int?): HuntingClubMemberInvitationOperationResponse()
}

interface HuntingClubMemberInvitationUpdater {
    suspend fun acceptHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse

    suspend fun rejectHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse
}

internal class HuntingClubMemberInvitationNetworkUpdater(
    private val backendApiProvider: BackendApiProvider,
) : HuntingClubMemberInvitationUpdater {
    override suspend fun acceptHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {

        val response = backendApiProvider.backendAPI.acceptHuntingClubMemberInvitation(invitationId)
        response.onError { statusCode, _ ->
            return HuntingClubMemberInvitationOperationResponse.Failure(statusCode)
        }
        return HuntingClubMemberInvitationOperationResponse.Success
    }

    override suspend fun rejectHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        val response = backendApiProvider.backendAPI.rejectHuntingClubMemberInvitation(invitationId)
        response.onError { statusCode, _ ->
            return HuntingClubMemberInvitationOperationResponse.Failure(statusCode)
        }
        return HuntingClubMemberInvitationOperationResponse.Success
    }
}
