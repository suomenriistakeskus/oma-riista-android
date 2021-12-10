package fi.riista.common.logging

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*

class NetworkCallLogging {

    companion object Feature : HttpClientFeature<NetworkCallLogging, NetworkCallLogging> {
        override val key: AttributeKey<NetworkCallLogging> = AttributeKey("NetworkCallLogging")

        override fun prepare(block: NetworkCallLogging.() -> Unit): NetworkCallLogging =
            NetworkCallLogging().apply(block)

        override fun install(feature: NetworkCallLogging, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                logger.log(logLevel) { "${context.method.value} ${context.url.buildString()} -->" }
                proceed()
            }
            scope.receivePipeline.intercept(HttpReceivePipeline.After) {
                val response = context.response
                logger.log(logLevel) { "<-- ${response.status} ${response.request.url}" }
                proceed()
            }
        }

        private val logger: Logger by getLogger("Network")
        private val logLevel = LogLevel.VERBOSE
    }
}