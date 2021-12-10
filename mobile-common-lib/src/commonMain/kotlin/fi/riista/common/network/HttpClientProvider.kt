package fi.riista.common.network

import fi.riista.common.RiistaSdkConfiguration
import io.ktor.client.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*

internal expect class HttpClientProvider() {
    fun getConfiguredHttpClient(sdkConfiguration: RiistaSdkConfiguration,
                                cookiesStorage: CookiesStorage): HttpClient
}

@Suppress("unused") // having HttpClientProvider as receiver provides better scoping
internal fun HttpClientProvider.configureDefaultRequest(
    requestBuilder: HttpRequestBuilder,
    sdkConfiguration: RiistaSdkConfiguration
) {
    requestBuilder.header("mobileClientVersion", sdkConfiguration.versionInfo.appVersion)
    requestBuilder.header("platform", sdkConfiguration.platform.name.platformNameString)
    requestBuilder.header("device", sdkConfiguration.device.name)
    requestBuilder.header("osVersion", sdkConfiguration.device.osVersion)
}