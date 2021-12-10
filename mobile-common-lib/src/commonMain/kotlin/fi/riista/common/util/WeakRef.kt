package fi.riista.common.util

/**
 * There's a WeakReference on android and iOS but those are not available in common code.
 * Since we're not going to be running on js, let's expect those implementations to be
 * available.
 */
expect class WeakRef<T : Any>(referred: T) {
    fun get(): T?

    fun clear()
}