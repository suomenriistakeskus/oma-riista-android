package fi.riista.common.domain.srva.sync.network

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*

internal class DeleteSrvaEvent(
    private val eventRemoteId: Long
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        return client.request(
            request = {
                delete(urlString = "${client.serverBaseAddress}/api/mobile/v2/srva/srvaevent/$eventRemoteId") {
                }
            },
            configureResponseHandler = {
                onSuccess {
                    NetworkResponse.SuccessWithNoData(statusCode = it.status.value)
                }
            }
        )
    }
}
