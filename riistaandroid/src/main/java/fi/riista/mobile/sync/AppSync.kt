package fi.riista.mobile.sync

import android.os.Handler
import android.util.Log
import fi.riista.common.RiistaSDK
import fi.riista.common.logging.getLogger
import fi.riista.common.reactive.AppObservable
import fi.riista.mobile.AppConfig
import fi.riista.mobile.AppLifecycleHandler
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.Authenticator.AuthSuccessCallback
import fi.riista.mobile.utils.CredentialsStore
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.network.SynchronizedCookieStore
import fi.vincit.androidutilslib.task.TextTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppSync @Inject constructor(
    @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext,
    private val syncConfig: SyncConfig,
    private val announcementSync: AnnouncementSync,
    private val metsaHallitusPermitSync: MetsaHallitusPermitSync,
    private val localImageRemover: LocalImageRemover,
    private val permitManager: PermitManager,
    private val appLifecycleHandler: AppLifecycleHandler,
    private val cookieStore: SynchronizedCookieStore,
    private val credentialsStore: CredentialsStore,
    private val authenticator: Authenticator,
) {

    interface AppSyncListener {
        fun onSyncStarted()
        fun onSyncCompleted()
    }

    enum class SyncPrecondition {
        /**
         * Network has been at least once reachable
         */
        CONNECTED_TO_NETWORK,

        /**
         * Has the automatic sync been enabled?
         */
        AUTOMATIC_SYNC_ENABLED,

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

        private val requiredForManualSync: Boolean
            get() {
                return when (this) {
                    CONNECTED_TO_NETWORK,
                    AUTOMATIC_SYNC_ENABLED,
                    USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY -> false
                    CREDENTIALS_VERIFIED,
                    HOME_SCREEN_REACHED,
                    DATABASE_MIGRATION_FINISHED -> true
                }
            }

        private val requiredForAutomaticSync: Boolean
            get() {
                return when (this) {
                    CONNECTED_TO_NETWORK,
                    AUTOMATIC_SYNC_ENABLED,
                    USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY,
                    CREDENTIALS_VERIFIED,
                    HOME_SCREEN_REACHED,
                    DATABASE_MIGRATION_FINISHED -> true
                }
            }

        /**
         * Should the automatic sync be performed immediately when precondition is enabled
         * (assuming other conditions are met)?
         */
        val triggersImmediateAutomaticSyncWhenEnabled: Boolean
            get() {
                return when (this) {
                    USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY -> false
                    CONNECTED_TO_NETWORK,
                    AUTOMATIC_SYNC_ENABLED,
                    CREDENTIALS_VERIFIED,
                    HOME_SCREEN_REACHED,
                    DATABASE_MIGRATION_FINISHED -> true
                }
            }

        fun requiredFor(syncMode: SyncMode): Boolean =
            when (syncMode) {
                SyncMode.SYNC_MANUAL ->     requiredForManualSync
                SyncMode.SYNC_AUTOMATIC ->  requiredForAutomaticSync
            }

        companion object {
            fun getRequiredFor(syncMode: SyncMode): Set<SyncPrecondition> {
                return values().filter {
                        it.requiredFor(syncMode = syncMode)
                    }.toSet()
            }
        }

    }

    private var syncPreconditions = mutableSetOf<SyncPrecondition>()

    private val syncHandler = Handler()
    private val syncTask: Runnable

    private val syncListeners: MutableList<AppSyncListener> = CopyOnWriteArrayList()

    private var isSyncQueued = false

    @Volatile
    private var isSyncRunning = false

    /**
     * Is the manual synchronization possible?
     */
    val manualSynchronizationPossible: AppObservable<Boolean> = AppObservable(false)

    init {
        syncTask = Runnable {
            setSyncRunning(running = true)

            syncListeners.forEach { it.onSyncStarted() }

            fetchGameLogYearsAndThenSyncAll(true)
        }

        if (syncConfig.isAutomatic()) {
            enableSyncPrecondition(SyncPrecondition.AUTOMATIC_SYNC_ENABLED)
        }

        enableSyncPrecondition(SyncPrecondition.USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY)
    }

    fun addSyncListener(listener: AppSyncListener, notifyImmediately: Boolean) {
        syncListeners.add(listener)

        if (notifyImmediately) {
            if (isSyncRunning) {
                listener.onSyncStarted()
            } else {
                listener.onSyncCompleted()
            }
        }
    }

    fun removeSyncListener(listener: AppSyncListener) {
        syncListeners.remove(listener)
    }

    @Synchronized
    private fun setSyncRunning(running: Boolean) {
        isSyncRunning = running
        updateManualSynchronizationPossible()
    }

    @Synchronized
    fun enableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_AUTOMATIC
        enableSyncPrecondition(SyncPrecondition.AUTOMATIC_SYNC_ENABLED)
    }

    @Synchronized
    fun disableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_MANUAL
        disableSyncPrecondition(SyncPrecondition.AUTOMATIC_SYNC_ENABLED)
    }


    /**
     * Enables given [SyncPrecondition] and queues sync if all preconditions are met.
     */
    @Synchronized
    fun enableSyncPrecondition(precondition: SyncPrecondition) {
        if (!syncPreconditions.contains(precondition)) {
            logger.v { "Enable sync precondition: $precondition" }
            syncPreconditions.add(precondition)
            updateManualSynchronizationPossible()
        } else {
            logger.v { "Sync precondition $precondition already enabled!" }
        }

        // try to queue automatic sync even if sync condition was already met beforehand
        // -> this ensures automatic sync is queued e.g. when resuming the app as HOME_SCREEN_REACHED
        //    probably won't be cleared when MainActivity is paused
        queueAutomaticSyncIfPreconditionsMet(
            synchronizeImmediately = precondition.triggersImmediateAutomaticSyncWhenEnabled
        )
    }

    /**
     * Disables the given [SyncPrecondition]. Also ensures sync won't continue running.
     */
    @Synchronized
    fun disableSyncPrecondition(precondition: SyncPrecondition) {
        if (syncPreconditions.contains(precondition)) {
            logger.v { "Disable sync precondition: $precondition. Stopping automatic sync." }
            syncPreconditions.remove(precondition)
            updateManualSynchronizationPossible()

            if (!areSyncPreconditionsMet(syncMode = SyncMode.SYNC_AUTOMATIC)) {
                stopAutomaticSync()
            }
        } else {
            logger.v { "Sync precondition $precondition was already disabled" }
        }
    }

    @Synchronized
    fun synchronizeUsing(syncMode: SyncMode): Boolean {
        return if (areSyncPreconditionsMet(syncMode)) {
            syncImmediately()
        } else {
            logger.d { "Refusing to synchronize, preconditions not met" }
            false
        }
    }

    fun syncImmediatelyIfAutomaticSyncEnabled() {
        if (areSyncPreconditionsMet(syncMode = SyncMode.SYNC_AUTOMATIC)) {
            syncImmediately()
        }
    }

    /**
     * Starts app sync if it is not already running.
     *
     * @return Boolean value indicating whether sync was triggered.
     */
    @Synchronized
    fun syncImmediately(): Boolean {
        if (!isSyncRunning) {
            syncHandler.removeCallbacks(syncTask)
            isSyncQueued = true
            syncHandler.post(syncTask)
            return true
        }
        return false
    }

    private fun updateManualSynchronizationPossible() {
        manualSynchronizationPossible.set(
            value = !isSyncRunning && areSyncPreconditionsMet(syncMode = SyncMode.SYNC_MANUAL)
        )
    }

    private fun queueAutomaticSyncIfPreconditionsMet(synchronizeImmediately: Boolean) {
        if (areSyncPreconditionsMet(syncMode = SyncMode.SYNC_AUTOMATIC)) {
            queueAutomaticSync(synchronizeImmediately)
        }
    }

    private fun areSyncPreconditionsMet(syncMode: SyncMode): Boolean {
        val requiredSyncPreconditions = SyncPrecondition.getRequiredFor(syncMode)
        return (requiredSyncPreconditions - syncPreconditions).isEmpty().also {
            logger.v { "Sync preconditions met for $syncMode = $it" }
        }
    }

    /**
     * Synchronizes harvests, observations, SRVA events, announcements, Metsähallitus permits and images.
     *
     * This function is called on init and when the synchronization settings change.
     */
    @Synchronized
    private fun queueAutomaticSync(synchronizeImmediately: Boolean) {
        if (!syncConfig.isAutomatic()) {
            logger.v { "Not queueing sync (automatic sync disabled)" }
            return
        }

        if (!isSyncQueued) {
            logger.v { "Queueing automatic sync to be performed" }
            isSyncQueued = true
            val delayMillis = if (synchronizeImmediately) {
                500L // not immediate but almost
            } else {
                SYNC_INTERVAL_MILLISECONDS
            }

            syncHandler.postDelayed(syncTask, delayMillis)
        } else {
            logger.v { "Automatic sync already queued" }
        }
    }

    @Synchronized
    private fun stopAutomaticSync() {
        syncHandler.removeCallbacks(syncTask)
        isSyncQueued = false
    }

    /**
     * Fetches first game log years to apply for harvest and observation sync.
     * Then starts to perform full app sync.
     *
     * @param retry - Whether to try again after failure and subsequent successful login
     */
    private fun fetchGameLogYearsAndThenSyncAll(retry: Boolean) {
        val yearTask: GameYearFetchingTask = object : GameYearFetchingTask(syncWorkContext, cookieStore) {

            override fun onFinishText(userInfoText: String) {
                handleGameLogYearsResponse(userInfoText)
            }

            override fun onError() {
                // If error happened to any other reason than network being unreachable, try logging in again
                if (httpStatusCode != -1 && retry) {
                    tryLogin(httpStatusCode)
                } else {
                    Log.d(TAG, "Fetching game log years failed")
                    completeSync()
                }
            }
        }
        yearTask.start()
    }

    private fun handleGameLogYearsResponse(userInfoText: String) = CoroutineScope(Main).launch {
        // TODO Pre-loading permits is considered to be moved out of AppSync. Originally, it is intended that
        //  pre-loading takes place right after user account info is loaded. Hence, this is currently done here.
        preloadPermits()

        try {
            // Continue processing in a coroutine dispatcher optimized for CPU intensive work.
            withContext(Default) {
                val userInfoJson = JSONObject(userInfoText)
                val username = userInfoJson.getString("username")

                syncAll(username)
            }
        } catch (e: JSONException) {
            Log.d(TAG, "Error while parsing user info JSON: ${e.message}")
            completeSync()
        } catch (e: Exception) {
            Log.d(TAG, "Error occurred while doing app sync: $e")
            completeSync()
        }
    }

    // Preload permits in IO thread.
    private suspend fun preloadPermits() = withContext(IO) { permitManager.preloadPermits() }

    /**
     * Synchronizes announcements, observations, SRVA events, Metsähallitus Permits, harvests and finally images.
     *
     * @param username - Username of the logged in user
     */
    private suspend fun syncAll(username: String) = withContext(Main) {

        RiistaSDK.synchronizeAllDataPieces()

        announcementSync.sync {
            metsaHallitusPermitSync.sync(username) {
                localImageRemover.removeDeletedImagesLocallyAsync()
                completeSync()
            }
        }
    }

    private fun completeSync() {
        syncListeners.forEach { it.onSyncCompleted() }

        val inForeground = appLifecycleHandler.isApplicationInForeground
        var automaticSyncEnabled: Boolean

        synchronized(this) {
            automaticSyncEnabled = syncConfig.isAutomatic()

            // Only continue further syncs when app is active and automatic sync is enabled.

            if (inForeground && automaticSyncEnabled) {
                isSyncQueued = true
                syncHandler.postDelayed(syncTask, SYNC_INTERVAL_MILLISECONDS)
            } else {
                isSyncQueued = false
            }

            setSyncRunning(running = false)
        }

        if (!inForeground) {
            Log.d(TAG, "Not queuing next sync because app not in foreground")
        } else if (!automaticSyncEnabled) {
            Log.d(TAG, "Not queuing next sync because automatic sync is not enabled")
        }
    }

    /**
     * Tries to login using stored credentials if not already logged in
     * If the login doesn't succeed, user is forced to log out
     */
    private fun tryLogin(statusCode: Int) {
        if (credentialsStore.isCredentialsSaved() && statusCode == 401) {
            CoroutineScope(Main).launch {
                authenticator.reauthenticate(
                    callback = object : AuthSuccessCallback() {
                        override fun onLoginSuccessful(userInfo: String?) {
                            // Try syncing once again.
                            fetchGameLogYearsAndThenSyncAll(false)
                        }
                    },
                    timeoutSeconds = Authenticator.DEFAULT_AUTHENTICATION_TIMEOUT_SECONDS
                )
            }
        } else {
            fetchGameLogYearsAndThenSyncAll(false)
        }
    }

    private open class GameYearFetchingTask(
        workContext: WorkContext,
        cookieStore: SynchronizedCookieStore
    ) : TextTask(workContext, AppConfig.getBaseUrl() + "/gamediary/account") {

        init {
            this.cookieStore = cookieStore
            httpMethod = HttpMethod.GET
        }

        override fun onFinishText(userInfoText: String) {
            // Override
        }
    }

    companion object {

        private const val TAG = "AppSync"
        private const val SYNC_INTERVAL_MILLISECONDS = 5 * 60 * 1000L

        private val logger by getLogger(AppSync::class)
    }
}
