package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestAttemptDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchShootingTestAttempt(
    private val shootingTestAttemptId: Long,
): NetworkRequest<ShootingTestAttemptDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestAttemptDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/attempt/$shootingTestAttemptId"
        return client.request(
            request = {
                get(urlString = url) {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}

