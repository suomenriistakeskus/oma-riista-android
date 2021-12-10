package fi.riista.common.authentication.passwordReset

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

internal class RequestPasswordReset(
    private val passwordResetDTO: RequestPasswordResetDTO,
): NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val payload = passwordResetDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize password reset dto to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/password/reset") {
                    contentType(ContentType.Application.Json)
                    body = payload
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