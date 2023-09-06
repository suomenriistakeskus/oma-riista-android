package fi.riista.common.domain.harvest.sync.network

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*

class DeleteHarvestImage(
    private val imageUuid: String,
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        return client.request(
            request = {
                // Note: observation has the same URL. If you are changing this then check that also.
                delete(urlString = "${client.serverBaseAddress}/api/mobile/v2/gamediary/image/$imageUuid") {
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
