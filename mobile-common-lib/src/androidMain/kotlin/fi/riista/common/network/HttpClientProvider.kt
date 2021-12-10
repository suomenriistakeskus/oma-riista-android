package fi.riista.common.network

import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.logging.NetworkCallLogging
import fi.riista.common.logging.getLogger
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import org.conscrypt.Conscrypt
import java.security.Security

internal actual class HttpClientProvider {
    actual fun getConfiguredHttpClient(sdkConfiguration: RiistaSdkConfiguration,
                                       cookiesStorage: CookiesStorage): HttpClient {
        // OkHttp will complain about missing Conscrypt unless it has been
        // added as the first security provider
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        } catch (e : Throwable) {
            logger.w { "Failed to add Conscrypt as first security provider. " +
                    "Exception: $e" }
        }

        return HttpClient(OkHttp) {
            engine {
                config {
                    // handle redirects manually. Don't let okhttp follow redirects
                    // as otherwise cookies are not stored
                    // https://github.com/ktorio/ktor/issues/1254
                    followRedirects(false)
                    followSslRedirects(false)

                    // we're seeing sporadic "unexpected end of stream on https://..." errors
                    // that are caused by java.io.EOFException: \n not found: limit=0 content=...
                    //
                    // There's a ktor issue which seems to match our case perfectly as we are
                    // using a ktor version that is affected:
                    // https://github.com/ktorio/ktor/issues/1708
                    //
                    // Fix the issue by retrying if a connection failure occurs
                    retryOnConnectionFailure(true)
                }
            }
            install(NetworkCallLogging) { }
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

    companion object {
        private val logger by getLogger("AndroidHttpClientProvider")
    }
}