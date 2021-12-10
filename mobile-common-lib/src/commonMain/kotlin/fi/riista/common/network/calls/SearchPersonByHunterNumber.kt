package fi.riista.common.network.calls

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.dto.PersonWithHunterNumberDTO
import fi.riista.common.network.NetworkClient
import io.ktor.client.request.*
import io.ktor.http.*

internal class SearchPersonByHunterNumber(
    private val hunterNumberDTO: HunterNumberDTO
): NetworkRequest<PersonWithHunterNumberDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<PersonWithHunterNumberDTO> {

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/search/person/hunternumber?hunterNumber=$hunterNumberDTO") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    body = ""
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
