package fi.riista.common.domain.huntingControl.sync

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.sync.dto.toLoadRhyHuntingControlEvents
import fi.riista.common.domain.huntingControl.sync.model.LoadRhyHuntingControlEvents
import fi.riista.common.model.LoadStatus
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData

internal class HuntingControlRhyFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
) : NetworkDataFetcher<LoadRhysAndHuntingControlEventsDTO>(),
    BackendApiProvider by backendApiProvider {

    val syncTimestamp: AtomicReference<LocalDateTime?> = AtomicReference(null)

    private val _rhys = mutableListOf<LoadRhyHuntingControlEvents>()
    val rhys: List<LoadRhyHuntingControlEvents>?
        get() {
            return if (_rhys.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _rhys
            }
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<LoadRhysAndHuntingControlEventsDTO> {
        return backendAPI.fetchHuntingControlRhys(syncTimestamp.get())
    }

    override fun handleSuccess(statusCode: Int, responseData: NetworkResponseData<out LoadRhysAndHuntingControlEventsDTO>) {
        val fetchedRhys = responseData.typed.map { it.toLoadRhyHuntingControlEvents(logger) }
        _rhys.clear()
        _rhys.addAll(fetchedRhys)
    }

    override fun handleError401() {
        _rhys.clear()
    }

    internal fun clear() {
        _rhys.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }
}
