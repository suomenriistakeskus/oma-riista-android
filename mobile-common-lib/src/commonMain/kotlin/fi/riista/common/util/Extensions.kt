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

internal fun <T> Collection<T>.hasSameElements(other: Collection<T>): Boolean {
    return toSet() == other.toSet()
}

/**
 * According to
 * https://discuss.kotlinlang.org/t/best-way-to-replace-an-element-of-an-immutable-list/8646/12
 */
internal inline fun <T> List<T>.replace(index: Int, item: T): List<T> {
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