package fi.riista.common.domain.huntingclub.ui

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.huntingclub.HuntingClubMemberInvitationOperationResponse
import fi.riista.common.domain.huntingclub.HuntingClubsContext
import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HuntingClubController(
    private val huntingClubsContext: HuntingClubsContext,
    private val languageProvider: LanguageProvider,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ListHuntingClubsViewModel>() {

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListHuntingClubsViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        huntingClubsContext.fetchMemberships(refresh)
        huntingClubsContext.fetchInvitations(refresh)

        val memberships = huntingClubsContext.huntingClubMembershipProvider.memberships
        val invitations = huntingClubsContext.huntingClubMemberInvitationProvider.invitations

        if (memberships == null || invitations == null) {
            emit(ViewModelLoadStatus.LoadFailed)

        } else {
            val invitationsHeader = HuntingClubViewModel.Header(
                id = "header_invitations".hashCode().toLong(),
                stringProvider.getString(RR.string.hunting_club_membership_invitations)
            )
            val huntingClubInvitations = invitations.map { invitation ->
                HuntingClubViewModel.Invitation(
                    id = "invitation_${invitation.id}".hashCode().toLong(),
                    name = invitation.club.name.localizedWithFallbacks(languageProvider) ?: "",
                    officialCode = invitation.club.officialCode,
                    invitationId = invitation.id,
                )
            }

            val membershipsHeader = HuntingClubViewModel.Header(
                id = "header_membership".hashCode().toLong(),
                stringProvider.getString(RR.string.hunting_club_memberships)
            )
            val huntingClubMemberships = memberships.map { membership ->
                HuntingClubViewModel.HuntingClub(
                    id = "membership_${membership.id}".hashCode().toLong(),
                    name = membership.organisation.name.localizedWithFallbacks(languageProvider) ?: "",
                    officialCode = membership.organisation.officialCode,
                )
            }

            emit(
                ViewModelLoadStatus.Loaded(
                    ListHuntingClubsViewModel(
                        items = listOfNotNull(invitationsHeader.takeUnless { huntingClubInvitations.isEmpty() }) +
                                huntingClubInvitations +
                                listOfNotNull(membershipsHeader.takeUnless { huntingClubMemberships.isEmpty() }) +
                                huntingClubMemberships,
                        hasOpenInvitations = huntingClubInvitations.isNotEmpty()
                    )
                )
            )
        }
    }

    suspend fun acceptInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        return huntingClubsContext.acceptInvitation(invitationId)
    }

    suspend fun rejectInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): HuntingClubMemberInvitationOperationResponse {
        return huntingClubsContext.rejectInvitation(invitationId)
    }
}
