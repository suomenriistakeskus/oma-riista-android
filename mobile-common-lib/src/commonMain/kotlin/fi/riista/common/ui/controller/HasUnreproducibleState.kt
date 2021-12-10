package fi.riista.common.ui.controller

/**
 * An interface for accessing unreproducible state. A unreproducible state is something
 * that can not otherwise be restored from network or from database. An example of such
 * state is e.g. something that user has entered as an input or settings used for filtering data.
 */
interface HasUnreproducibleState<State> {
    /**
     * Gets the current unreproducible state.
     */
    fun getUnreproducibleState(): State?

    /**
     * Restores the unreproducible state.
     */
    fun restoreUnreproducibleState(state: State)
}