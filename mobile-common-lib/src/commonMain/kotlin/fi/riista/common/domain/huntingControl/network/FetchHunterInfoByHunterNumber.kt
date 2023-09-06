package fi.riista.common.domain.huntingControl.network

import fi.riista.common.domain.huntingControl.dto.HuntingControlHunterInfoDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHunterInfoByHunterNumber(
    private val hunterNumber: String,
): NetworkRequest<HuntingControlHunterInfoDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingControlHunterInfoDTO> {

        return client.request(
            request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/hunterInfo?hunterNumber=${hunterNumber}") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
