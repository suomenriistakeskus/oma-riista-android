package fi.riista.common.authentication.registration

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class RequestStartRegistration(
    private val startRegistrationDTO: StartRegistrationDTO,
): NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val payload = startRegistrationDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize start registration dto to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/register/send-email") {
                    contentType(ContentType.Application.Json)
                    setBody(body = payload)
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