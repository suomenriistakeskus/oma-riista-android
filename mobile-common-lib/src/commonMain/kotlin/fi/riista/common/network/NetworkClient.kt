package fi.riista.common.network

import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.logging.getLogger
import fi.riista.common.network.calls.*
import fi.riista.common.network.cookies.CustomCookiesStorage
import fi.riista.common.util.JsonHelper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

class NetworkClient internal constructor(
    private val sdkConfiguration: RiistaSdkConfiguration
) {

    /**
     * A custom cookie storage which allows accessing all added cookies
     * from outside. Allows passing e.g. authentication cookies to network libraries
     * on the application side
     * -> login using RiistaSDK, rest of the network calls on application side.
     *
     * // TODO: remove once application network communication has been moved to RiistaSDK
     */
    val cookiesStorage = CustomCookiesStorage()

    val httpClient: HttpClient by lazy {
        HttpClientProvider().getConfiguredHttpClient(sdkConfiguration, cookiesStorage)
    }

    // The server base address without trailing slash ('/') e.g. "https://oma.riista.fi"
    internal val serverBaseAddress: String = sdkConfiguration.serverBaseAddress

    /**
     * Performs the given network request using this [NetworkClient] as client.
     */
    internal suspend fun <DataType> performRequest(networkRequest: NetworkRequest<DataType>) =
        networkRequest.request(client = this@NetworkClient)

    /**
     * Helps making requests by allowing the callee to configure how request is made and
     * optionally allows customizing response handling.
     *
     * By default the response is assumed to contain data and successful responses are reported
     * with [NetworkResponse.Success]. The response errors are also handled internally using either
     * [NetworkResponse.ResponseError] or [NetworkResponse.NetworkError] unless there is a custom
     * response handler for the response.
     *
     * Main points
     * - the actual request is specified by using given [request]
     * - will handle one redirection automatically
     * - custom response handling can be configured by [configureResponseHandler]
     */
    internal suspend inline fun <reified DataType> request(
        crossinline request: suspend HttpClient.() -> HttpResponse,
        crossinline configureResponseHandler: NetworkResponseHandler<DataType>.() -> Unit
    ): NetworkResponse<DataType> {
        val responseHandler = NetworkResponseHandlerImpl<DataType>(
                successHandler = createDefaultSuccessHandler()
        )
        configureDefaultErrorHandling(responseHandler)
        responseHandler.configureResponseHandler()

        var requestResult = kotlin.runCatching { httpClient.request() }
        for (redirect in 1..maxRedirectJumps) {
            requestResult.fold(
                    onSuccess = { response: HttpResponse ->
                        return responseHandler.handleSuccess(response)
                    },
                    onFailure = { exception: Throwable ->
                        when (exception) {
                            is RedirectResponseException -> {
                                requestResult = kotlin.runCatching {
                                    val httpResponse = performRedirect(exception.response)

                                    @Suppress("FoldInitializerAndIfToElvis") // better readability this way
                                    if (httpResponse == null) {
                                        // couldn't even attempt redirect, Location header missing
                                        return NetworkResponse.NetworkError(null)
                                    }
                                    httpResponse
                                }
                            }
                            is ResponseException ->
                                return responseHandler.handleResponseError(exception.response)
                            is CancellationException -> {
                                logger.w { "Network request was cancelled!" }
                                return NetworkResponse.Cancelled(exception)
                            }
                            else -> {
                                logger.w { "Failed to receive response at all! Exception: ${exception.message}" }
                                logger.v { "Stacktrace\n${exception.stackTraceToString()}" }
                                return NetworkResponse.NetworkError(exception)
                            }
                        }
                    }
            )
        }

        // max redirects reached
        return NetworkResponse.NetworkError(null)
    }

    private inline fun <reified DataType> createDefaultSuccessHandler(): StatusCodeHandler<DataType> {
        return { response ->
            val rawData: String = response.receive()
            val networkResponse: NetworkResponse<DataType> = try {
                val typedData = JsonHelper.deserializeFromJsonUnsafe<DataType>(rawData)
                NetworkResponse.Success(statusCode = response.status.value,
                                        data = NetworkResponseData(rawData, typedData))
            } catch (e : Throwable) {
                logger.w { "Failed to parse success response. Exception: ${e.message}" }
                NetworkResponse.OtherError(e)
            }

            networkResponse
        }
    }

    private fun <DataType> configureDefaultErrorHandling(responseHandler: NetworkResponseHandlerImpl<DataType>) {
        responseHandler.onOtherStatusCodes { response ->
            NetworkResponse.ResponseError(statusCode = response.status.value)
        }
    }

    /**
     * Attempts to redirect to the Location specified in the headers. Returns either
     * the HttpResponse from [HttpClient.get] or null if there was no [HttpHeaders.Location] header
     * in the response headers.
     *
     * The Http method is changed to GET in all cases i.e. does not respect response status code.
     *
     * Does not catch any exceptions i.e. all Ktor exceptions are thrown.
     */
    internal suspend fun performRedirect(
        response: HttpResponse
    ): HttpResponse? {
        response.headers[HttpHeaders.Location]?.let { url ->
            val originalAcceptHeader = response.request.headers[HttpHeaders.Accept]

            val redirectUrl: String? =
                if (url.startsWith("http")) {
                    if (sdkConfiguration.networkClientConfiguration.allowRedirectsToAbsoluteHosts) {
                        url
                    } else {
                        null
                    }
                } else {
                    serverBaseAddress + url
                }

            if (redirectUrl == null) {
                logger.w { "Not allowed to redirect to absolute url $url." }
                return null
            }

            logger.v { "Redirecting to $redirectUrl" }

            return httpClient.get(redirectUrl) {
                originalAcceptHeader?.let {
                    header(HttpHeaders.Accept, it)
                }
            }
        }

        logger.w { "No Location header for response status ${response.status}. " +
                "Url was ${response.request.url}" }
        return null
    }

    companion object {
        private val logger by getLogger(NetworkClient::class)
        private const val maxRedirectJumps = 10
    }
}
