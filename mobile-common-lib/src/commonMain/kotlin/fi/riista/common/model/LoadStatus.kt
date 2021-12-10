package fi.riista.common.model

/**
 * A rather generic class for indicating a load status for something.
 *
 * The inner classes are intentionally open which allows adding functionality to them while
 * maintaining compatibility with common load status handling.
 */
sealed class LoadStatus(
    val notLoaded: Boolean = false,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: Boolean = false,
) {
    open class NotLoaded : LoadStatus(notLoaded = true)
    open class Loading : LoadStatus(loading = true)
    // todo: consider adding _source_ (i.e. network or offline) to Loaded. Alternatively
    // inherit Loaded when necessary (e.g. LoadedFromNetwork / LoadedFromOffline)
    open class Loaded : LoadStatus(loaded = true)
    open class LoadError : LoadStatus(error = true)

    override fun toString(): String {
        return "LoadStatus(notLoaded=$notLoaded, loading=$loading, loaded=$loaded, error=$error)"
    }
}