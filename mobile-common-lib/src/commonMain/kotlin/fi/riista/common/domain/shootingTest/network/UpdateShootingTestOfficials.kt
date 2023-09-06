package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.UpdateShootingTestOfficialsDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class UpdateShootingTestOfficials(
    private val updateShootingTestOfficialsDTO: UpdateShootingTestOfficialsDTO
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/event/${updateShootingTestOfficialsDTO.shootingTestEventId}/officials"

        val payload = updateShootingTestOfficialsDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize update shooting test oficials data to json"
        }

        return client.request(
            request = {
                put(urlString = url) {
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
