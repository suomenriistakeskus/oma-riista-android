package fi.riista.common.domain.srva.metadata.network

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.metadata.dto.toSrvaMetadata
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface SrvaMetadataFetcher: DataFetcher {
    val metadata: SrvaMetadata?
}

internal class SrvaMetadataNetworkFetcher(
    backendApiProvider: BackendApiProvider,
) : SrvaMetadataFetcher,
    NetworkDataFetcher<SrvaMetadataDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _metadata = AtomicReference<SrvaMetadata?>(null)
    override val metadata: SrvaMetadata?
        get() {
            return _metadata.value
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<SrvaMetadataDTO> =
        backendAPI.fetchSrvaMetadata()

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out SrvaMetadataDTO>
    ) {
        _metadata.set(responseData.typed.toSrvaMetadata())
    }

    override fun handleError401() {
        _metadata.set(null)
    }
}
