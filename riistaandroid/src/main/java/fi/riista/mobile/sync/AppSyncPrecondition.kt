package fi.riista.mobile.sync

import fi.riista.common.network.sync.SynchronizationLevel

enum class AppSyncPrecondition {
    /**
     * Network has been at least once reachable
     */
    CONNECTED_TO_NETWORK,

    /**
     * Has the automatic user content sync been enabled?
     *
     * There's (almost) always a periodic sync but user content (harvests, observations, etc) won't be synced
     * automatically unless automatic user content synchronization is enabled.
     */
    AUTOMATIC_USER_CONTENT_SYNC_ENABLED,

    /**
     * Is the user doing something else than editing or creating an entry (harvest, observation, srva,
     * hunting control event) that will be synchronized during [AppSync]?
     *
     * Allows preventing synchronization while entry has been saved to database (and possibly synchronized
     * internally) and thus eliminating simultaneous synchronizations that could occur in rare
     * circumstances i.e. when AppSync is performed right when user is saving the entry.
     */
    USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY,

    /**
     * Credentials exist and preliminary tests show that they are valid i.e.
     * login call either succeeds or at least won't return 401 or 403.
     **/
    CREDENTIALS_VERIFIED,

    /**
     * UI has been navigated beyond login screen (appsync shouldn't be performed there)
     */
    HOME_SCREEN_REACHED,

    /**
     * Migrations from legacy app database to Riista SDK database has been run.
     */
    DATABASE_MIGRATION_FINISHED,
    ;

    /**
     * Should the periodic sync be started immediately when precondition is enabled
     * (assuming other conditions are met)?
     */
    val triggersPerformingImmediateSyncWhenEnabled: Boolean
        get() {
            return when (this) {
                USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY -> false
                // enabling automatic _user content_ sync should trigger immediate sync in order
                // to give user an impression that app reacted to user request
                AUTOMATIC_USER_CONTENT_SYNC_ENABLED,
                CONNECTED_TO_NETWORK,
                CREDENTIALS_VERIFIED,
                HOME_SCREEN_REACHED,
                DATABASE_MIGRATION_FINISHED -> true
            }
        }

    companion object {
        fun getRequiredFor(syncMode: SyncMode): Pair<String, Set<AppSyncPrecondition>> {
            return when (syncMode) {
                SyncMode.SYNC_MANUAL -> requiredForManualUserContentSync
                SyncMode.SYNC_AUTOMATIC -> requiredForAutomaticUserContentSync
            }
        }

        /**
         * Periodic synchronization is a synchronization that occurs periodically (obviously). It, however,
         * differs from automatic user content sync that is able to sync just metadata level data.
         *
         * See [SynchronizationLevel.METADATA] for more information.
         */
        val requiredForPeriodicSync: Pair<String, Set<AppSyncPrecondition>> by lazy {
            "periodic sync" to AppSyncPrecondition.values()
                .filter {
                    when (it) {
                        AUTOMATIC_USER_CONTENT_SYNC_ENABLED -> false
                        CONNECTED_TO_NETWORK,
                        USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY,
                        CREDENTIALS_VERIFIED,
                        HOME_SCREEN_REACHED,
                        DATABASE_MIGRATION_FINISHED -> true
                    }
                }.toSet()
        }

        private val requiredForAutomaticUserContentSync: Pair<String, Set<AppSyncPrecondition>> by lazy {
            "automatic sync" to AppSyncPrecondition.values()
                .filter {
                    when (it) {
                        CONNECTED_TO_NETWORK,
                        AUTOMATIC_USER_CONTENT_SYNC_ENABLED,
                        USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY,
                        CREDENTIALS_VERIFIED,
                        HOME_SCREEN_REACHED,
                        DATABASE_MIGRATION_FINISHED -> true
                    }
                }.toSet()
        }

        private val requiredForManualUserContentSync: Pair<String, Set<AppSyncPrecondition>> by lazy {
            "manual sync" to AppSyncPrecondition.values()
                .filter {
                    when (it) {
                        CONNECTED_TO_NETWORK,
                        AUTOMATIC_USER_CONTENT_SYNC_ENABLED,
                        USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY -> false
                        CREDENTIALS_VERIFIED,
                        HOME_SCREEN_REACHED,
                        DATABASE_MIGRATION_FINISHED -> true
                    }
                }.toSet()
        }
    }
}
