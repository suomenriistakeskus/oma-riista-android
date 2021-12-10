package fi.riista.common.util

import android.os.Bundle
import fi.riista.common.extensions.getLongOrNull

/**
 * Android [Bundle] is not mocked and thus not available in unit tests. Wrap its usage in
 * order to allow testing bundle usage in unit tests.
 */
internal interface BundleWrapper {
    fun putLong(key: String, value: Long)
    fun getLongOrNull(key: String): Long?
    fun putString(key: String, value: String)
    fun getString(key: String): String?
}

/**
 * Wraps the normal android [Bundle] and delegates all calls to it.
 *
 * Since android [Bundle] is not unit testable we have to rely on that delegation
 * works as expected.
 */
internal class WrappedBundle(private val bundle: Bundle): BundleWrapper {
    override fun putLong(key: String, value: Long) {
        bundle.putLong(key, value)
    }

    override fun getLongOrNull(key: String): Long? {
        return bundle.getLongOrNull(key)
    }

    override fun putString(key: String, value: String) {
        bundle.putString(key, value)
    }

    override fun getString(key: String): String? {
        return bundle.getString(key)
    }
}

internal class MockBundle: BundleWrapper {
    internal val values = mutableMapOf<String, Any>()

    override fun putLong(key: String, value: Long) {
        values[key] = value
    }

    override fun getLongOrNull(key: String): Long? {
        return valueOrNull<Long>(key)
    }

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getString(key: String): String? {
        return valueOrNull<String>(key)
    }

    private fun <T> valueOrNull(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return (values[key] as? T)
    }
}
