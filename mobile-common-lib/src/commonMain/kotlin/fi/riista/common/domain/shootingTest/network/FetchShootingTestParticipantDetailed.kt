package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantDetailedDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchShootingTestParticipantDetailed(
    private val participantId: Long,
): NetworkRequest<ShootingTestParticipantDetailedDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestParticipantDetailedDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/participant/$participantId/attempts"
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
