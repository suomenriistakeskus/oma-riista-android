package fi.riista.common.domain.observation.metadata.network

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.dto.toObservationMetadata
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface ObservationMetadataFetcher: DataFetcher {
    val metadata: ObservationMetadata?
}

internal class ObservationMetadataNetworkFetcher(
    backendApiProvider: BackendApiProvider,
) : ObservationMetadataFetcher,
    NetworkDataFetcher<ObservationMetadataDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _metadata = AtomicReference<ObservationMetadata?>(null)
    override val metadata: ObservationMetadata?
        get() {
            return _metadata.value
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<ObservationMetadataDTO> =
        backendAPI.fetchObservationMetadata()

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out ObservationMetadataDTO>
    ) {
        _metadata.set(responseData.typed.toObservationMetadata())
    }

    override fun handleError401() {
        _metadata.set(null)
    }
}
