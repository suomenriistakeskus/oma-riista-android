package fi.riista.mobile.sync

import android.os.Handler
import fi.riista.common.logging.getLogger
import fi.riista.common.network.sync.SynchronizationLevel
import fi.riista.common.reactive.AppObservable
import fi.riista.mobile.AppLifecycleHandler
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.CredentialsStore
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.network.SynchronizedCookieStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * A class which responsibility is to manage metadata and user content synchronization.
 *
 * Consists of:
 * - scheduling synchronization periodically
 * - managing the requirements for synchronization (periodic, automatic, manual)
 *
 * The actual synchronization is performed by the [Synchronization].
 */
@Singleton
class AppSync @Inject constructor(
    @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext,
    private val syncConfig: SyncConfig,
    private val announcementSync: AnnouncementSync,
    private val localImageRemover: LocalImageRemover,
    private val permitManager: PermitManager,
    private val appLifecycleHandler: AppLifecycleHandler,
    private val cookieStore: SynchronizedCookieStore,
    private val credentialsStore: CredentialsStore,
    private val authenticator: Authenticator,
) : Synchronization.Listener {

    interface AppSyncListener {
        fun onSynchronizationEvent(synchronizationEvent: SynchronizationEvent)

        /**
         * @param   isImmediateUserContentSync  Is the sync scheduled to be performed immediately after the current
         *                                      and is the sync user content sync?
         */
        fun onSynchronizationScheduled(isImmediateUserContentSync: Boolean)
    }


    private val syncPreconditions = mutableSetOf<AppSyncPrecondition>()

    private val syncHandler = Handler()
    private val syncTask: Runnable

    private val syncListeners: MutableList<AppSyncListener> = CopyOnWriteArrayList()

    private var isSyncQueued = false
    private val synchronizationFlagsForNextSync = CopyOnWriteArraySet<SynchronizationFlag>()


    /**
     * The current synchronization that is being performed. Only valid during synchronization.
     */
    @Volatile
    private var currentSynchronization: Synchronization? = null

    private val isSyncRunning: Boolean
        get() {
            return currentSynchronization != null
        }

    /**
     * The last synchronization event that has occurred (if any).
     *
     * If a sync listener is added during sync, it can be notified about the last thing that happened.
     */
    @Volatile
    private var lastSynchronizationEvent: SynchronizationEvent? = null

    /**
     * Is the manual synchronization possible?
     */
    val manualSynchronizationPossible: AppObservable<Boolean> = AppObservable(false)

    init {
        syncTask = Runnable {
            val syncMode = syncConfig.syncMode

            val synchronization = Synchronization.create(
                flags = synchronizationFlagsForNextSync,
                syncMode = syncMode,
                syncModePreconditionsMet = areSyncPreconditionsMet(syncMode),
                syncWorkContext = syncWorkContext,
                cookieStore = cookieStore,
                announcementSync = announcementSync,
                localImageRemover = localImageRemover,
                permitManager = permitManager,
                listener = this
            )

            startSynchronization(synchronization)
        }

        if (syncConfig.isAutomatic()) {
            enableSyncPrecondition(AppSyncPrecondition.AUTOMATIC_USER_CONTENT_SYNC_ENABLED)
        }

        enableSyncPrecondition(AppSyncPrecondition.USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY)
    }


    fun addSyncListener(listener: AppSyncListener, notifyImmediately: Boolean) {
        syncListeners.add(listener)

        if (notifyImmediately) {
            lastSynchronizationEvent?.let {
                listener.onSynchronizationEvent(it)
            }
        }
    }

    fun removeSyncListener(listener: AppSyncListener) {
        syncListeners.remove(listener)
    }

    @Synchronized
    fun enableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_AUTOMATIC
        enableSyncPrecondition(AppSyncPrecondition.AUTOMATIC_USER_CONTENT_SYNC_ENABLED)

    }

    @Synchronized
    fun disableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_MANUAL
        disableSyncPrecondition(AppSyncPrecondition.AUTOMATIC_USER_CONTENT_SYNC_ENABLED)
    }


    /**
     * Enables given [AppSyncPrecondition] and queues sync if all preconditions are met.
     */
    @Synchronized
    fun enableSyncPrecondition(precondition: AppSyncPrecondition) {
        if (!syncPreconditions.contains(precondition)) {
            logger.v { "Enable sync precondition: $precondition" }
            syncPreconditions.add(precondition)
            updateManualSynchronizationPossible()
        } else {
            logger.v { "Sync precondition $precondition already enabled!" }
        }

        // try to queue periodic sync even if sync condition was already met beforehand
        // -> this ensures periodic sync is queued e.g. when resuming the app as HOME_SCREEN_REACHED
        //    probably won't be cleared when MainActivity is paused
        queuePeriodicSyncIfPreconditionsMet(
            synchronizeImmediately = precondition.triggersPerformingImmediateSyncWhenEnabled
        )
    }

    /**
     * Disables the given [AppSyncPrecondition]. Also ensures sync won't continue running.
     */
    @Synchronized
    fun disableSyncPrecondition(precondition: AppSyncPrecondition) {
        if (syncPreconditions.contains(precondition)) {
            logger.v { "Disable sync precondition: $precondition." }
            syncPreconditions.remove(precondition)
            updateManualSynchronizationPossible()

            if (!areSyncPreconditionsMet(requiredSyncPreconditions = AppSyncPrecondition.requiredForPeriodicSync)) {
                stopPeriodicSync()
            }
        } else {
            logger.v { "Sync precondition $precondition was already disabled" }
        }
    }

    @Synchronized
    fun scheduleImmediateSyncUsing(syncMode: SyncMode): Boolean {
        return if (areSyncPreconditionsMet(syncMode)) {
            scheduleImmediateSync(forceUserContentSync = true, forceContentReload = false)
            true
        } else {
            logger.d { "Refusing to synchronize, preconditions not met" }
            false
        }
    }

    fun scheduleImmediateSyncIfAutomaticSyncEnabled() {
        if (areSyncPreconditionsMet(syncMode = SyncMode.SYNC_AUTOMATIC)) {
            scheduleImmediateSync(forceUserContentSync = true, forceContentReload = false)
        }
    }

    /**
     * Starts app sync if it is not already running. Otherwise schedules a sync to be performed
     * immediately after the current sync.
     *
     * @param   forceUserContentSync    Should the user content be synchronized? (manual sync)
     * @param   forceContentReload      Should all content be reloaded?
     */
    @Synchronized
    fun scheduleImmediateSync(forceUserContentSync: Boolean, forceContentReload: Boolean) {
        if (forceUserContentSync) {
            synchronizationFlagsForNextSync.add(SynchronizationFlag.FORCE_USER_CONTENT_SYNC)
        }
        if (forceContentReload) {
            synchronizationFlagsForNextSync.add((SynchronizationFlag.FORCE_CONTENT_RELOAD))
        }

        if (!isSyncRunning) {
            logger.v { "Sync not running: scheduling immediate sync" }
            syncHandler.removeCallbacks(syncTask)
            isSyncQueued = true
            syncHandler.post(syncTask)
        } else {
            logger.v { "Sync running: creating a pending sync request" }
            synchronizationFlagsForNextSync.add(SynchronizationFlag.SYNC_IMMEDIATELY_AFTER_CURRENT_SYNC)
            updateManualSynchronizationPossible()
        }

        val isImmediateUserContentSync = synchronizationFlagsForNextSync.isPendingUserContentSync
        syncListeners.forEach {
            it.onSynchronizationScheduled(isImmediateUserContentSync)
        }
    }




    private fun updateManualSynchronizationPossible() {
        val userContentSyncIsRunning = currentSynchronization?.let { synchronization ->
            synchronization.synchronizationLevel == SynchronizationLevel.USER_CONTENT
        } ?: false

        val userContentSyncAboutToStart = synchronizationFlagsForNextSync.containsAll(setOf(
            SynchronizationFlag.SYNC_IMMEDIATELY_AFTER_CURRENT_SYNC,
            SynchronizationFlag.FORCE_USER_CONTENT_SYNC
        ))

        manualSynchronizationPossible.set(
            // allow manual synchronization even when metadata sync is running
            // - if user requests manual sync it can then be set as a pending sync request
            value = !userContentSyncIsRunning && !userContentSyncAboutToStart &&
                    areSyncPreconditionsMet(syncMode = SyncMode.SYNC_MANUAL)
        )
    }

    private fun queuePeriodicSyncIfPreconditionsMet(synchronizeImmediately: Boolean) {
        if (areSyncPreconditionsMet(requiredSyncPreconditions = AppSyncPrecondition.requiredForPeriodicSync)) {
            queuePeriodicSync(synchronizeImmediately)
        }
    }

    private fun areSyncPreconditionsMet(requiredSyncPreconditions: Pair<String, Set<AppSyncPrecondition>>): Boolean {
        return (requiredSyncPreconditions.second - syncPreconditions).isEmpty().also {
            logger.v { "Sync preconditions met for ${requiredSyncPreconditions.first} = $it" }
        }
    }

    private fun areSyncPreconditionsMet(syncMode: SyncMode): Boolean {
        return areSyncPreconditionsMet(requiredSyncPreconditions = AppSyncPrecondition.getRequiredFor(syncMode))
    }

    /**
     * Queues periodic synchronization.
     *
     * @param   synchronizeImmediately  If true will remove the possibly queued sync task. Will not cancel
     *                                  running sync task however.
     */
    @Synchronized
    private fun queuePeriodicSync(synchronizeImmediately: Boolean) {
        if (isSyncRunning) {
            logger.v { "Sync already running, not queuing periodic sync." }
            return
        }

        if (synchronizeImmediately) {
            logger.v { "Queuing immediate periodic sync." }
            scheduleImmediateSync(forceUserContentSync = false, forceContentReload = false)
            return
        }

        if (!isSyncQueued) {
            logger.v { "Queueing periodic sync to be performed" }
            isSyncQueued = true
            syncHandler.postDelayed(syncTask, SYNC_INTERVAL_MILLISECONDS)
        } else {
            logger.v { "Periodic sync already queued, nothing to do" }
        }
    }

    @Synchronized
    private fun stopPeriodicSync() {
        logger.v { "Stopping periodic sync." }
        syncHandler.removeCallbacks(syncTask)
        isSyncQueued = false
    }



    override fun onSynchronizationEvent(event: SynchronizationEvent) {
        logger.v { "Synchronization event: $event" }

        if (event is SynchronizationEvent.Completed) {
            completeCurrentSynchronizationAndScheduleNext()
        }

        updateManualSynchronizationPossible()

        // notify about completion first..
        setLastSynchronizationEventAndNotifyListeners(event)

        // .. and then notify about scheduled one
        if (event is SynchronizationEvent.Completed && isSyncQueued) {
            val isImmediateUserContentSync = synchronizationFlagsForNextSync.isPendingUserContentSync
            syncListeners.forEach {
                it.onSynchronizationScheduled(isImmediateUserContentSync)
            }
        }
    }

    override fun onSynchronizationNetworkError(httpStatusCode: Int) {
        val synchronization = currentSynchronization ?: kotlin.run {
            logger.e { "Got synchronization network error but there's no currentSynchronization!" }
            return
        }

        if (credentialsStore.isCredentialsSaved() && httpStatusCode == 401) {
            logger.v { "Got authentication error during synchronization, attempting to re-login.." }
            tryLoginAndContinueSynchronizationIfSuccessful(synchronization)
        } else {
            logger.v { "Got error (statuscode = $httpStatusCode) during synchronization, resuming.." }
            synchronization.resumeAfterNetworkError()
        }
    }

    private fun startSynchronization(synchronization: Synchronization) {
        synchronized (this) {
            currentSynchronization = synchronization

            // clear the synchronization flags for next sync as they are now applied
            synchronizationFlagsForNextSync.clear()
        }

        synchronization.start()
    }

    private fun setLastSynchronizationEventAndNotifyListeners(synchronizationEvent: SynchronizationEvent) {
        synchronized(this) {
            lastSynchronizationEvent = synchronizationEvent
        }

        syncListeners.forEach {
            it.onSynchronizationEvent(synchronizationEvent)
        }
    }

    /**
     * Tries to login using stored credentials if not already logged in
     * If the login doesn't succeed, user is forced to log out
     */
    private fun tryLoginAndContinueSynchronizationIfSuccessful(synchronization: Synchronization) {
        CoroutineScope(Main).launch {
            authenticator.reauthenticate(
                callback = object : Authenticator.AuthCallback {
                    override fun onLoginSuccessful(userInfo: String?) {
                        synchronization.resumeAfterNetworkError()
                    }

                    override fun onLoginFailed(httpStatusCode: Int) {
                        // handle login failure as well!
                        when (httpStatusCode) {
                            401, 403 -> return // nop, handled in Authenticator
                            else -> synchronization.cancel()
                        }

                    }
                },
                timeoutSeconds = Authenticator.DEFAULT_AUTHENTICATION_TIMEOUT_SECONDS
            )
        }
    }

    private fun completeCurrentSynchronizationAndScheduleNext() {
        val inForeground = appLifecycleHandler.isApplicationInForeground
        var periodicSyncPreconditionsMet: Boolean

        synchronized(this) {
            periodicSyncPreconditionsMet = areSyncPreconditionsMet(
                requiredSyncPreconditions = AppSyncPrecondition.requiredForPeriodicSync,
            )
            val pendingImmediateSync = synchronizationFlagsForNextSync.contains(
                SynchronizationFlag.SYNC_IMMEDIATELY_AFTER_CURRENT_SYNC
            )

            // Only continue further syncs when app is active and preconditions for periodic sync are met.

            if (inForeground && periodicSyncPreconditionsMet) {
                isSyncQueued = true
                val interval = when (pendingImmediateSync) {
                    true -> SYNC_INTERVAL_MILLISECONDS_IMMEDIATE
                    false -> SYNC_INTERVAL_MILLISECONDS
                }
                syncHandler.postDelayed(syncTask, interval)
            } else {
                isSyncQueued = false
            }

            currentSynchronization = null
        }

        if (!inForeground) {
            logger.d { "Not queuing next periodic sync because app is not in foreground" }
        } else if (!periodicSyncPreconditionsMet) {
            logger.d { "Not queuing next periodic sync because preconditions are not met" }
        }
    }

    companion object {

        private const val SYNC_INTERVAL_MILLISECONDS = 5 * 60 * 1000L
        private const val SYNC_INTERVAL_MILLISECONDS_IMMEDIATE = 250L

        private val logger by getLogger(AppSync::class)
    }
}
