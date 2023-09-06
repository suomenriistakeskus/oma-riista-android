package fi.riista.common.domain.huntingclub

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingclub.invitations.HuntingClubMemberInvitationFromNetworkProvider
import fi.riista.common.domain.huntingclub.invitations.HuntingClubMemberInvitationNetworkUpdater
import fi.riista.common.domain.huntingclub.invitations.HuntingClubMemberInvitationOperationResponse
import fi.riista.common.domain.huntingclub.invitations.HuntingClubMemberInvitationProvider
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitation
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitationId
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig

/**
 * Context for hunting club membership invitations.
 */
class HuntingClubsContext internal constructor(
    backendApiProvider: BackendApiProvider,
): BackendApiProvider by backendApiProvider {

    private val _huntingClubMemberInvitationProvider = HuntingClubMemberInvitationFromNetworkProvider(backendApiProvider)
    val huntingClubMemberInvitationProvider: HuntingClubMemberInvitationProvider = _huntingClubMemberInvitationProvider

    private val _huntingClubMemberInvitationUpdater = HuntingClubMemberInvitationNetworkUpdater(backendApiProvider)

    val invitations: List<HuntingClubMemberInvitation>?
        get() = huntingClubMemberInvitationProvider.invitations


    suspend fun fetchInvitations(refresh: Boolean = false) {
        huntingClubMemberInvitationProvider.fetch(refresh = refresh)
    }

    suspend fun acceptInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        val response = _huntingClubMemberInvitationUpdater.acceptHuntingClubMemberInvitation(invitationId)
        if (response == HuntingClubMemberInvitationOperationResponse.Success) {
            refreshMemberships()
            huntingClubMemberInvitationProvider.fetch(refresh = true)
        }
        return response
    }

    suspend fun rejectInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        val response = _huntingClubMemberInvitationUpdater.rejectHuntingClubMemberInvitation(invitationId)
        if (response == HuntingClubMemberInvitationOperationResponse.Success) {
            refreshMemberships()
            huntingClubMemberInvitationProvider.fetch(refresh = true)
        }
        return response
    }

    private suspend fun refreshMemberships() {
        RiistaSDK.synchronize(
            syncDataPiece = SyncDataPiece.HUNTING_CLUB_OCCUPATIONS,
            config = SynchronizationConfig(
                forceContentReload = true
            )
        )
    }

    fun clear() {
        _huntingClubMemberInvitationProvider.clear()
    }
}
