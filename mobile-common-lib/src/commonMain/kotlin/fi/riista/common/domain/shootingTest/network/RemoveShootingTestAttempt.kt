package fi.riista.common.domain.shootingTest.network

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*

internal class RemoveShootingTestAttempt(
    private val attemptId: Long,
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/attempt/$attemptId"
        return client.request(
            request = {
                delete(urlString = url) {
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

