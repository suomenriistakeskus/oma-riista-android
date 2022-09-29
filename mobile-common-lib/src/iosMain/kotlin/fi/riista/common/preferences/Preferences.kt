package fi.riista.common.preferences

import fi.riista.common.logging.getLogger
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults
import platform.Foundation.numberWithInt


actual class PlatformPreferences actual constructor(): Preferences {
    private val userDefaults: NSUserDefaults
        get() {
            return NSUserDefaults.standardUserDefaults
        }

    actual override fun contains(key: String): Boolean {
        val keysAndValues = userDefaults.dictionaryRepresentation()
        return keysAndValues.containsKey(key)
    }

    actual override fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
    }

    actual override fun getString(key: String, defaultValue: String?): String? {
        return when (val value = userDefaults.objectForKey(key)) {
            is String -> value
            else -> {
                logger.w { "No String for key $key" }
                defaultValue
            }
        }
    }

    actual override fun putInt(key: String, value: Int) {
        // getting Int out of NSUserDefaults is a bit problematic. We do get NSInteger
        // but converting that to Int wasn't so easy as NSInteger is either 32bit or 64bit
        // depending on platform
        // - there's toInt() function for Long but not for Int -> Not sure whether
        //   userDefaults.integerForKey(key).toInt() would work on 32bit platforms
        //
        // As a result of all this, store Ints as NSNumber as that _should_
        // work on all platforms
        userDefaults.setObject(NSNumber.numberWithInt(value), forKey = key)
    }

    actual override fun getInt(key: String, defaultValue: Int?): Int? {
        return when (val number = userDefaults.objectForKey(key)) {
            is NSNumber -> number.intValue
            else -> {
                logger.w { "No Int for key $key" }
                defaultValue
            }
        }
    }

    actual override fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, forKey = key)
    }

    actual override fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
    }

    actual override fun getBoolean(key: String, defaultValue: Boolean?): Boolean? {
        // userDefaults boolForKey will return NO (= false) if key doesn't exist.
        // -> guard with contains check
        return if (contains(key)) {
            // actually the value may be something else than Boolean but for that
            // we cannot do much.. at least easily.
            userDefaults.boolForKey(key)
        } else {
            logger.w { "No Boolean for key $key" }
            defaultValue
        }
    }

    companion object {
        private val logger by getLogger(Preferences::class)
    }
}
