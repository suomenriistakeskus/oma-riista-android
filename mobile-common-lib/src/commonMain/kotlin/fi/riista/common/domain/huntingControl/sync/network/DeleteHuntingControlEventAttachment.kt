package fi.riista.common.domain.huntingControl.sync.network

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*

internal class DeleteHuntingControlEventAttachment(
    private val attachmentId: Long,
) : NetworkRequest<Unit> {
    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        return client.request(
            request = {
                delete(urlString = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/attachment/$attachmentId") {
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
