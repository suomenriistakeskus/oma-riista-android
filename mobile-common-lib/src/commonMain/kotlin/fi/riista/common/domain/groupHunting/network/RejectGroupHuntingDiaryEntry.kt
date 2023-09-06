package fi.riista.common.domain.groupHunting.network

import fi.riista.common.domain.groupHunting.dto.RejectDiaryEntryDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class RejectGroupHuntingDiaryEntry(
    private val rejectDiaryEntryDTO: RejectDiaryEntryDTO
) : NetworkRequest<Unit> {
    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        val groupId = rejectDiaryEntryDTO.id
        val payload = rejectDiaryEntryDTO.serializeToJson()

        requireNotNull(payload) {
            "Failed to serialize reject diary entry to json"
        }

        return client.request(
                request = {
                    post(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/$groupId/rejectentry") {
                        contentType(ContentType.Application.Json)
                        setBody(body = payload)
                    }
                },
                configureResponseHandler = {
                    onSuccess { httpResponse ->
                        NetworkResponse.SuccessWithNoData(httpResponse.status.value)
                    }
                }
        )
    }
}
