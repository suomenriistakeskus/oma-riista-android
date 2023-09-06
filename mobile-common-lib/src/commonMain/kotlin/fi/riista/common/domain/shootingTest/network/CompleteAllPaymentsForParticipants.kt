package fi.riista.common.domain.shootingTest.network

import fi.riista.common.model.Revision
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

internal class CompleteAllPaymentsForParticipants(
    private val participantId: Long,
    private val rev: Revision,
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/participant/$participantId/payment"

        val dto = RevisionDTO(rev)
        val payload = dto.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize revision data to json"
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

@Serializable
private data class RevisionDTO(
    val rev: Int,
)
