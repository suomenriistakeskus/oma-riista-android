package fi.riista.common.util


internal fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean) {
    firstOrNull(predicate)?.let {
        remove(it)
    }
}

/**
 * Returns the first and only element in the collection. Returns null if there are no elements or
 * more than one element in the collection.
 */
internal fun <T> Collection<T>.firstAndOnly(): T? {
    return if (this.size == 1) {
        first()
    } else {
        null
    }
}

/**
 * Calls the given [block] if [other] is not null. Returns the [block] result or null.
 */
fun <T, U, R> T.letWith(other: U?, block: (T, U) -> R?): R? {
    return if (other != null) {
        block(this, other)
    } else {
        null
    }
}

/**
 * Calls the given [block] if [condition] is true. Returns the [block] result or null.
 */
inline fun <T, R> T.letIf(condition: Boolean, block: (T) -> R?): R? {
    return if (condition) {
        block(this)
    } else {
        null
    }
}

fun <T> T?.isNullOr(other: T): Boolean {
    return this == null || this == other
}