package fi.riista.common.huntingclub

import fi.riista.common.huntingclub.model.HuntingClubMemberInvitation
import fi.riista.common.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.model.Occupation
import fi.riista.common.network.BackendApiProvider

/**
 * Context for hunting club memberships and hunting club membership invitations.
 */
class HuntingClubsContext internal constructor(
    backendApiProvider: BackendApiProvider,
): BackendApiProvider by backendApiProvider {

    private val _huntingClubMembershipProvider = HuntingClubMembershipFromNetworkProvider(backendApiProvider)
    val huntingClubMembershipProvider: HuntingClubMembershipProvider = _huntingClubMembershipProvider

    private val _huntingClubMemberInvitationProvider = HuntingClubMemberInvitationFromNetworkProvider(backendApiProvider)
    val huntingClubMemberInvitationProvider: HuntingClubMemberInvitationProvider = _huntingClubMemberInvitationProvider

    private val _huntingClubMemberInvitationUpdater = HuntingClubMemberInvitationNetworkUpdater(backendApiProvider)

    val memberships: List<Occupation>?
        get() = huntingClubMembershipProvider.memberships

    val invitations: List<HuntingClubMemberInvitation>?
        get() = huntingClubMemberInvitationProvider.invitations

    suspend fun fetchMemberships(refresh: Boolean = false) {
        huntingClubMembershipProvider.fetch(refresh = refresh)
    }

    suspend fun fetchInvitations(refresh: Boolean = false) {
        huntingClubMemberInvitationProvider.fetch(refresh = refresh)
    }

    suspend fun acceptInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        val response = _huntingClubMemberInvitationUpdater.acceptHuntingClubMemberInvitation(invitationId)
        if (response == HuntingClubMemberInvitationOperationResponse.Success) {
            huntingClubMembershipProvider.fetch(refresh = true)
            huntingClubMemberInvitationProvider.fetch(refresh = true)
        }
        return response
    }

    suspend fun rejectInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        val response = _huntingClubMemberInvitationUpdater.rejectHuntingClubMemberInvitation(invitationId)
        if (response == HuntingClubMemberInvitationOperationResponse.Success) {
            huntingClubMembershipProvider.fetch(refresh = true)
            huntingClubMemberInvitationProvider.fetch(refresh = true)
        }
        return response
    }

    fun clear() {
        _huntingClubMembershipProvider.clear()
        _huntingClubMemberInvitationProvider.clear()
    }
}
