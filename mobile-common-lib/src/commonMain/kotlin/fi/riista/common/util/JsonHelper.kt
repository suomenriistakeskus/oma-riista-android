package fi.riista.common.util

import fi.riista.common.logging.getLogger
import fi.riista.common.preferences.Preferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object JsonHelper {
    val configuredJson = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> deserializeFromJsonUnsafe(json: String): T {
        return configuredJson.decodeFromString(json)
    }

    inline fun <reified T> deserializeFromJson(json: String): T? {
        return try {
            deserializeFromJsonUnsafe(json)
        } catch (e : Throwable) {
            logger.w { "Failed to deserialize json ${json.take(32)}.. " +
                    "Exception was ${e.message}" }
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> serializeToJsonUnsafe(value: T): String {
        // default configuration is sufficient for now
        return Json.encodeToString(value)
    }

    inline fun <reified T> serializeToJson(value: T): String? {
        return try {
            serializeToJsonUnsafe(value)
        } catch (e : Throwable) {
            logger.w { "Failed to serialize an object of type ${T::class.simpleName ?: "<unknown>"}." +
                    " Exception was ${e.message}" }
            null
        }
    }

    val logger by getLogger(JsonHelper::class)
}

inline fun <reified T> T.serializeToJson(): String? =
    JsonHelper.serializeToJson(this)

inline fun <reified T> String?.deserializeFromJson(): T? {
    if (this.isNullOrBlank()) {
        return null
    }

    return JsonHelper.deserializeFromJson(this)
}


internal inline fun <reified T> Preferences.putJson(key: String, value: T) {
    value.serializeToJson()?.let { json ->
        putString(key, json)
    }
}

internal inline fun <reified T> Preferences.getJson(key: String): T? {
    return getString(key, defaultValue = null)?.let { json ->
        json.deserializeFromJson()
    }
}
