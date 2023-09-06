package fi.riista.common.network.sync


data class SynchronizationConfig(
    /**
     * Should the content be reloaded no matter what?
     */
    val forceContentReload: Boolean,
) {
    companion object {
        val DEFAULT = SynchronizationConfig(
            forceContentReload = false
        )
    }
}

