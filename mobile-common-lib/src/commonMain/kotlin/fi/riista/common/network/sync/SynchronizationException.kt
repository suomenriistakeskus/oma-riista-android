package fi.riista.common.network.sync

/**
 * An exception that wraps any exceptions thrown during synchronization operation
 */
class SynchronizationException(message: String?, cause: Throwable?): Exception(message, cause)
