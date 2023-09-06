package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestPaymentUpdateDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class UpdatePaymentStateForParticipant(
    private val participantId: Long,
    private val paymentUpdateDTO: ShootingTestPaymentUpdateDTO
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/participant/$participantId/payment"

        val payload = paymentUpdateDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize shooting test payment update data to json"
        }

        return client.request(
            request = {
                post(urlString = url) {
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
