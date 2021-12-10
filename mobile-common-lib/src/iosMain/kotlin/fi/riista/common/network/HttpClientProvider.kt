package fi.riista.common.network

import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.logging.NetworkCallLogging
import io.ktor.client.*
import io.ktor.client.engine.ios.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*

internal actual class HttpClientProvider {
    actual fun getConfiguredHttpClient(sdkConfiguration: RiistaSdkConfiguration,
                                       cookiesStorage: CookiesStorage): HttpClient {
        return HttpClient(Ios) {
            engine {
                configureRequest {
                    setAllowsCellularAccess(true)
                }
            }
            install(NetworkCallLogging) {}
            install(UserAgent) {
                agent = sdkConfiguration.userAgent
            }
            install(HttpCookies) {
                storage = cookiesStorage
            }
            defaultRequest {
                configureDefaultRequest(this, sdkConfiguration)
            }
        }
    }
}