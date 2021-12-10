package fi.riista.common.model


/**
 * An interface wrapping the entity id on the backend. Each id-class should
 * implement this interface.
 *
 * The idea is to wrap backend ids in order to better support local entities.
 */
interface EntityId {
    /**
     * The id of the entity on the backend. `null` if entity has not
     * yet been stored to backend.
     */
    val remoteId: BackendId?

    /**
     * Converts the entity id to [Long]
     */
    fun toLong(): Long
}