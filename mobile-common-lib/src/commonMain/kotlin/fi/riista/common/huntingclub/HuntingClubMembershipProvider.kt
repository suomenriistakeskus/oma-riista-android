package fi.riista.common.huntingclub

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.dto.toOccupation
import fi.riista.common.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.model.Occupation
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface HuntingClubMembershipProvider: DataFetcher {
    val memberships: List<Occupation>?
}

internal class HuntingClubMembershipFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
) : HuntingClubMembershipProvider,
    NetworkDataFetcher<HuntingClubMembershipsDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _memberships = AtomicReference<List<Occupation>?>(null)
    override val memberships: List<Occupation>?
        get() = _memberships.value

    override suspend fun fetchFromNetwork(): NetworkResponse<HuntingClubMembershipsDTO> =
        backendAPI.fetchHuntingClubMemberships()

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out HuntingClubMembershipsDTO>
    ) {
        val fetchedMemberships = responseData.typed.map { it.toOccupation() }
        _memberships.set(fetchedMemberships)
    }

    override fun handleError401() {
        _memberships.set(null)
    }

    internal fun clear() {
        _memberships.set(null)
        loadStatus.set(LoadStatus.NotLoaded())
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(HuntingClubMembershipFromNetworkProvider::class)
    }
}
