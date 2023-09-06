package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantsDTO
import fi.riista.common.domain.shootingTest.model.ShootingTestEventId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchShootingTestParticipants(
    private val shootingTestEventId: ShootingTestEventId,
) : NetworkRequest<ShootingTestParticipantsDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestParticipantsDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/event/$shootingTestEventId/participants?unfinishedOnly=false"
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
