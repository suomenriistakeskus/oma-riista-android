package fi.riista.common.network

import fi.riista.common.model.LoadStatus
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher
import fi.riista.common.util.SequentialDataFetcher

/**
 * A [DataFetcher] that fetches the data from the network. Only allows one simultaneous
 * data fetch at any time (see [SequentialDataFetcher])
 */
abstract class NetworkDataFetcher<DataType>: SequentialDataFetcher() {

    override suspend fun doFetch() {
        logger()?.v { "Initiating fetch from network.."}

        val fetchResponse = fetchFromNetwork()

        logger()?.v { "Network fetch completed, handling response.." }

        fetchResponse.onSuccess { statusCode, responseData ->
            handleSuccess(statusCode, responseData)

            logger()?.v { "Fetch completed successfully" }
            loadStatus.set(LoadStatus.Loaded())
        }

        fetchResponse.onError { statusCode, exception ->
            if (statusCode == 401) {
                handleError401()
            } else {
                handleError(statusCode, exception)
            }

            logger()?.v { "Failed to fetch (statusCode: $statusCode)" }

            // todo: consider passing statuscode + exception to LoadError
            loadStatus.set(LoadStatus.LoadError())
        }

        fetchResponse.onCancel {
            handleCancellation()

            logger()?.v { "Fetching from the network was cancelled." }

            loadStatus.set(LoadStatus.LoadError())
        }
    }

    abstract suspend fun fetchFromNetwork(): NetworkResponse<DataType>

    abstract fun handleSuccess(statusCode: Int, responseData: NetworkResponseData<out DataType>)

    /**
     * Special case handling for 401 / Unauthorized as this is probably the case that needs
     * to be handled in most cases any way.
     */
    abstract fun handleError401()

    open fun handleError(statusCode: Int?, exception: Throwable?) {}

    open fun handleCancellation() {}
}