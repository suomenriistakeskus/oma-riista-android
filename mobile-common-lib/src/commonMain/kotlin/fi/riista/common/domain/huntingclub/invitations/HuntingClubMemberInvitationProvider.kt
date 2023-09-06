package fi.riista.common.domain.huntingclub.invitations

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.huntingclub.invitations.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.invitations.dto.toHuntingClubMemberInvitation
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitation
import fi.riista.common.model.LoadStatus
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface HuntingClubMemberInvitationProvider: DataFetcher {
    val invitations: List<HuntingClubMemberInvitation>?
}

internal class HuntingClubMemberInvitationFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
) : HuntingClubMemberInvitationProvider,
    NetworkDataFetcher<HuntingClubMemberInvitationsDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _invitations = AtomicReference<List<HuntingClubMemberInvitation>?>(null)
    override val invitations: List<HuntingClubMemberInvitation>?
        get() = _invitations.value

    override suspend fun fetchFromNetwork(): NetworkResponse<HuntingClubMemberInvitationsDTO> =
        backendAPI.fetchHuntingClubMemberInvitations()

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out HuntingClubMemberInvitationsDTO>
    ) {
        val fetchedInvitations = responseData.typed.map { it.toHuntingClubMemberInvitation() }
        _invitations.set(fetchedInvitations)
    }

    override fun handleError401() {
        _invitations.set(null)
    }

    internal fun clear() {
        _invitations.set(null)
        loadStatus.set(LoadStatus.NotLoaded())
    }
}
