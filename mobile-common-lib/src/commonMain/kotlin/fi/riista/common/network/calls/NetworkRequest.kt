package fi.riista.common.network.calls

import fi.riista.common.network.NetworkClient

internal interface NetworkRequest<DataType> {
    /**
     * The request should now be performed. Call e.g. [NetworkClient.request] and
     * configure the request and response handling.
     */
    suspend fun request(client: NetworkClient): NetworkResponse<DataType>
}