package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestOfficialsDTO
import fi.riista.common.domain.shootingTest.model.ShootingTestEventId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchSelectedShootingTestOfficialsForEvent(
    private val shootingTestEventId: ShootingTestEventId,
) : NetworkRequest<ShootingTestOfficialsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestOfficialsDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/event/$shootingTestEventId/assignedofficials/"
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
