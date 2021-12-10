package fi.riista.common.preferences

import co.touchlab.stately.collections.IsoMutableMap
import fi.riista.common.logging.getLogger

class MockPreferences: Preferences {
    internal val container = IsoMutableMap<String, Any>()

    override fun contains(key: String): Boolean {
        return container.containsKey(key).also {
            logger.v { "Check if key $key exists: $it. Value: ${container[key]}" }
        }
    }

    override fun putString(key: String, value: String) {
        logger.v { "Storing string $key = $value" }
        container[key] = value
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return (container[key] as? String ?: defaultValue).also {
            logger.v { "Returned string for $key = $it" }
        }
    }

    override fun putInt(key: String, value: Int) {
        logger.v { "Storing int $key = $value" }
        container[key] = value
    }

    override fun getInt(key: String, defaultValue: Int?): Int? {
        return (container[key] as? Int ?: defaultValue).also {
            logger.v { "Returned int for $key = $it" }
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        logger.v { "Storing boolean $key = $value" }
        container[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean?): Boolean? {
        return (container[key] as? Boolean ?: defaultValue).also {
            logger.v { "Returned boolean for $key = $it" }
        }
    }

    companion object {
        private val logger by getLogger(MockPreferences::class)
    }
}