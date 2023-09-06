package fi.riista.common.network

import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.logging.NetworkCallLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest

internal actual class HttpClientProvider {
    actual fun getConfiguredHttpClient(
        sdkConfiguration: RiistaSdkConfiguration,
        cookiesStorage: CookiesStorage,
    ): HttpClient {
        return HttpClient(Darwin) {
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
            install(HttpTimeout)
            defaultRequest {
                configureDefaultRequest(this, sdkConfiguration)
            }

            // expect all requests to succeed and thus receive exceptions for non-2xx responses
            expectSuccess = true
        }
    }
}