package fi.riista.common.domain.groupHunting

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.groupHunting.dto.HuntingGroupAreaDTO
import fi.riista.common.domain.groupHunting.dto.toHuntingGroupArea
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface HuntingGroupAreaProvider: DataFetcher {
    val area: HuntingGroupArea?
}

internal class HuntingGroupAreaFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : HuntingGroupAreaProvider,
    NetworkDataFetcher<HuntingGroupAreaDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _area = AtomicReference<HuntingGroupArea?>(null)
    override val area: HuntingGroupArea?
        get() {
            return _area.value
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<HuntingGroupAreaDTO> =
        backendAPI.fetchHuntingGroupArea(huntingGroupId)

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out HuntingGroupAreaDTO>
    ) {
        _area.set(responseData.typed.toHuntingGroupArea())
    }

    override fun handleError401() {
        _area.set(null)
    }
}
