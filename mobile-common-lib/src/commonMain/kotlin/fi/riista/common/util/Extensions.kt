package fi.riista.common.util

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.use


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
 * Checks whether `this` contains an element matching the given [predicate]. Returns `true` if does and `false` if not.
 */
inline fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean {
    return firstOrNull(predicate) != null
}

internal fun <T> Collection<T>.hasSameElements(other: Collection<T>): Boolean {
    return toSet() == other.toSet()
}

/**
 * Checks whether this [Iterable] contains any of the items in the [others] [Iterable].
 *
 * Explicitly use [Iterable] as [others] type instead of vararg parameter. This ensures that
 * types match. With varargs it would have been possible to check e.g.
 *
 *   listOf(1, 2).containsAny("foo")
 *
 * as it assumes T == Comparable<*> and there wouldn't be a compiler error. Prevent possible
 * mistakes like that by always requiring an [Iterable].
 */
fun <T> Iterable<T>.containsAny(others: Iterable<T>): Boolean {
    val found = others.firstOrNull { other ->
        contains(other)
    }

    return found != null
}

/**
 * According to
 * https://discuss.kotlinlang.org/t/best-way-to-replace-an-element-of-an-immutable-list/8646/12
 */
internal fun <T> List<T>.replace(index: Int, item: T): List<T> {
    return if (index >= 0 && index < count()) {
        toMutableList().apply {
            this[index] = item
        }
    } else {
        this
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

fun <T> List<T>.withNumberOfElements(
    numberOfElements: Int,
    createElementBlock: () -> T
) : List<T> {
    val sanitizedNumberOfElement = numberOfElements.coerceAtLeast(0)

    if (sanitizedNumberOfElement == count()) {
        return this
    }
    else if (sanitizedNumberOfElement < count()) {
        return dropLast(count() - sanitizedNumberOfElement)
    }

    val result = toMutableList()
    while (sanitizedNumberOfElement > result.count()) {
        result.add(createElementBlock())
    }
    return result
}

fun Any?.toStringOrMissingIndicator(): String {
    return this.toStringOrToFallback("-")
}

fun Any?.toStringOrToFallback(fallback: String): String {
    if (this == null) {
        return fallback
    }

    val result = toString()
    return if (result.isNotBlank()) {
        result
    } else {
        fallback
    }
}

fun String.prefixed(prefix: String): String {
    return "$prefix$this"
}


internal fun <RowType : Any> Query<RowType>.executeAsSet(): Set<RowType> {
    val result = mutableSetOf<RowType>()
    execute().use {
        while (it.next()) result.add(mapper(it))
    }
    return result
}
