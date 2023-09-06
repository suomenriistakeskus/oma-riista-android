package fi.riista.common.authentication.login

import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.Parameters

internal class Login(
    private val username: String,
    private val password: String,
    private val timeoutSeconds: Int,
): NetworkRequest<UserInfoDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<UserInfoDTO> {
        return client.request(
                request = {
                    submitForm(
                            url = "${client.serverBaseAddress}/login",
                            formParameters = Parameters.build {
                                append("username", username)
                                append("password", password)
                                append("remember-me", "true")
                                append("client", "mobileapiv2")
                            },
                            // don't encode to query params --> use Post body instead
                            encodeInQuery = false
                    ) {
                        accept(ContentType.Application.Json)
                        timeout {
                            // iOS doesn't support connection timeout do don't use it
                            requestTimeoutMillis = this@Login.timeoutSeconds * 1000L
                            socketTimeoutMillis = this@Login.timeoutSeconds * 1000L
                        }
                    }
                },
                configureResponseHandler = {
                    // nop, default response handling works just fine
                }
        )
    }
}