package fi.riista.common.domain.srva.metadata.network

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*


internal class FetchSrvaMetadata : NetworkRequest<SrvaMetadataDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<SrvaMetadataDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/srva/parameters?srvaEventSpecVersion=${Constants.SRVA_SPEC_VERSION}"

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