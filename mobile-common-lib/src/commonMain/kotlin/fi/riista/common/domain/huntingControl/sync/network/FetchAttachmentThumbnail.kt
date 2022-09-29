package fi.riista.common.domain.huntingControl.sync.network

import fi.riista.common.domain.huntingControl.model.encodeToBase64
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import io.ktor.client.request.*
import io.ktor.client.statement.*

internal class FetchAttachmentThumbnail(
    private val attachmentId: Long,
) : NetworkRequest<ByteArray> {
    override suspend fun request(client: NetworkClient): NetworkResponse<ByteArray> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/attachment/${attachmentId}/thumbnail"
        return client.request(
            request = {
                get(urlString = url)
            },
            configureResponseHandler = {
                onSuccess { response ->
                    val data: ByteArray = response.readBytes()
                    NetworkResponse.Success(
                        statusCode = response.status.value,
                        data = NetworkResponseData(raw = data.encodeToBase64() ?: "", typed = data)
                    )
                }
            }
        )
    }
}
