package fi.riista.mobile.utils

/**
 * Keeps track when map caches should be cleared.
 */
object CacheClearTracker {
    private var background = false
    private var vector = false

    fun markBackgroundCacheToBeCleared() {
        background = true
    }

    fun markVectorCachesToBeCleared() {
        vector = true
    }

    @JvmStatic
    fun shouldClearBackgroundCache(): Boolean {
        val should = background
        background = false
        return should
    }

    @JvmStatic
    fun shouldClearVectorCaches(): Boolean {
        val should = vector
        vector = false
        return should
    }
}
