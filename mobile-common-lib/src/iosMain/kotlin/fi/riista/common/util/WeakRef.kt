package fi.riista.common.util

import kotlin.native.ref.WeakReference

actual class WeakRef<T : Any> actual constructor(referred: T) {
    private val delegate = WeakReference(referred)

    actual fun get(): T? = delegate.get()

    actual fun clear() = delegate.clear()
}