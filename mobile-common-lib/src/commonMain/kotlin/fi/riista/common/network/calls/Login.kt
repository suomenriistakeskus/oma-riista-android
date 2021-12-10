package fi.riista.common.network.calls

import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.network.NetworkClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.*
import io.ktor.http.*

internal class Login(private val username: String,
                     private val password: String): NetworkRequest<UserInfoDTO> {

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
                    }
                },
                configureResponseHandler = {
                    // nop, default response handling works just fine
                }
        )
    }
}