package fi.riista.common.domain.huntingControl.sync.network

import fi.riista.common.io.CommonFile
import fi.riista.common.io.appendFile
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.forms.*
import io.ktor.http.*

internal class UploadHuntingControlEventAttachment(
    private val eventId: Long,
    private val uuid: String,
    private val fileName: String,
    private val contentType: String,
    private val file: CommonFile,
) : NetworkRequest<Long> {
    override suspend fun request(client: NetworkClient): NetworkResponse<Long> {
        return client.request(
            request = {
                submitFormWithBinaryData(
                    url = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/attachment/$eventId/upload",
                    formData = formData {
                        append("uuid", uuid)
                        appendFile(
                            key = "file",
                            file = file,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=$fileName")
                            }
                        )
                    }
                )
            },
            configureResponseHandler = {
            }
        )
    }
}
