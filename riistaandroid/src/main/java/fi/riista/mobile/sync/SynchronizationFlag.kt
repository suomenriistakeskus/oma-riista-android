package fi.riista.mobile.sync

enum class SynchronizationFlag {
    SYNC_IMMEDIATELY_AFTER_CURRENT_SYNC,
    FORCE_USER_CONTENT_SYNC,
    FORCE_CONTENT_RELOAD,
}

val Set<SynchronizationFlag>.isPendingUserContentSync: Boolean
    get() = this.contains(SynchronizationFlag.SYNC_IMMEDIATELY_AFTER_CURRENT_SYNC) &&
            this.contains(SynchronizationFlag.FORCE_USER_CONTENT_SYNC)
