package fi.riista.common.logging

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.request
import io.ktor.util.AttributeKey

class NetworkCallLogging {

    // TODO: refactor to ktor 2.0 format
    companion object Feature : HttpClientPlugin<NetworkCallLogging, NetworkCallLogging> {
        override val key: AttributeKey<NetworkCallLogging> = AttributeKey("NetworkCallLogging")

        override fun prepare(block: NetworkCallLogging.() -> Unit): NetworkCallLogging =
            NetworkCallLogging().apply(block)

        override fun install(plugin: NetworkCallLogging, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                logger.log(logLevel) { "${context.method.value} ${context.url.buildString()} -->" }
                proceed()
            }
            scope.receivePipeline.intercept(HttpReceivePipeline.After) {response ->
                logger.log(logLevel) { "<-- ${response.status} ${response.request.url}" }
                proceed()
            }
        }

        private val logger: Logger by getLogger("Network")
        private val logLevel = LogLevel.VERBOSE
    }
}