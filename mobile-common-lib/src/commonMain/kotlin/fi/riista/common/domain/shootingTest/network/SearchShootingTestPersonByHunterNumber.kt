package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestPersonDTO
import fi.riista.common.domain.shootingTest.model.ShootingTestEventId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

internal class SearchShootingTestPersonByHunterNumber(
    private val shootingTestEventId: ShootingTestEventId,
    private val hunterNumber: String,
) : NetworkRequest<ShootingTestPersonDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestPersonDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/event/$shootingTestEventId/findhunter/hunternumber"

        val payload = SearchByHunterNumberDTO(hunterNumber).serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize search by hunter number data to json"
        }

        return client.request(
            request = {
                post(urlString = url) {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(body = payload)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}

@Serializable
private data class SearchByHunterNumberDTO(
    val hunterNumber: String,
)
