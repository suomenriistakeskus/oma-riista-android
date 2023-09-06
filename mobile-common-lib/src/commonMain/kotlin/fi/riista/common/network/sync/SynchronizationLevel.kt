package fi.riista.common.network.sync

enum class SynchronizationLevel(
    // specify level value explicitly instead of using ordinal just in case someone changes the order of enum constants
    private val level: Int
) {
    /**
     * Metadata and other information that is required to be kept up-to-date in order to enable correct behaviour.
     * This data includes data such as observation metadata, harvest seasons etc.
     */
    METADATA(level = 1),

    /**
     * User created content such as harvests, observations, srvas, points-of-interest etc.
     *
     * Using this as synchronization level will synchronize also the [METADATA].
     */
    USER_CONTENT(level = 2),
    ;

    fun isIncludedIn(synchronizationLevel: SynchronizationLevel): Boolean {
        return level <= synchronizationLevel.level
    }
}
