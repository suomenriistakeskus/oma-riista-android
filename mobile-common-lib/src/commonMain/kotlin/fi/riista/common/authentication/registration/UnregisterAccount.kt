package fi.riista.common.authentication.registration

import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*

internal class UnregisterAccount: NetworkRequest<LocalDateTimeDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<LocalDateTimeDTO> {
        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/unregister")
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
