package fi.riista.mobile.sync

import android.os.Handler
import android.util.Log
import fi.riista.common.RiistaSDK
import fi.riista.common.userInfo.LoginStatus
import fi.riista.mobile.AppConfig
import fi.riista.mobile.AppLifecycleHandler
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.service.harvest.HarvestEventEmitter
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.Authenticator.AuthSuccessCallback
import fi.riista.mobile.utils.CredentialsStore
import fi.riista.mobile.utils.Utils
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.network.SynchronizedCookieStore
import fi.vincit.androidutilslib.task.TextTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class AppSync @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext,
        private val syncConfig: SyncConfig,
        private val harvestSync: HarvestSync,
        private val observationSync: ObservationSync,
        private val announcementSync: AnnouncementSync,
        private val metsaHallitusPermitSync: MetsaHallitusPermitSync,
        private val localImageRemover: LocalImageRemover,
        private val harvestDatabase: HarvestDatabase,
        private val harvestEventEmitter: HarvestEventEmitter,
        private val permitManager: PermitManager,
        private val appLifecycleHandler: AppLifecycleHandler,
        private val cookieStore: SynchronizedCookieStore,
        private val credentialsStore: CredentialsStore,
        private val authenticator: Authenticator) {

    interface AppSyncListener {
        fun onSyncStarted()
        fun onSyncCompleted()
    }

    private val srvaSync = SrvaSync(syncWorkContext)

    private val syncHandler = Handler()
    private val syncTask: Runnable

    private val syncListeners: MutableList<AppSyncListener> = CopyOnWriteArrayList()

    private var isSyncQueued = false

    @Volatile
    private var isSyncRunning = false

    init {
        syncTask = Runnable {
            isSyncRunning = true

            syncListeners.forEach { it.onSyncStarted() }

            fetchGameLogYearsAndThenSyncAll(true)
        }
    }

    fun addSyncListener(listener: AppSyncListener) {
        syncListeners.add(listener)

        if (isSyncRunning) {
            listener.onSyncStarted()
        } else {
            listener.onSyncCompleted()
        }
    }

    fun removeSyncListener(listener: AppSyncListener) {
        syncListeners.remove(listener)
    }

    fun isSyncRunning() = isSyncRunning

    @Synchronized
    fun enableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_AUTOMATIC
        initSyncInternal(0)
    }

    @Synchronized
    fun disableAutomaticSync() {
        syncConfig.syncMode = SyncMode.SYNC_MANUAL
        stopAutomaticSyncInternal()
    }

    /**
     * Synchronizes harvests, observations, SRVA events, announcements, Metsähallitus permits and images.
     *
     * This function is called on init and when the synchronization settings change.
     *
     * @param initialWait Wait time before making first synchronization in milliseconds
     */
    @Synchronized
    fun initAutomaticSync(initialWait: Int) {
        if (syncConfig.isAutomatic()) {
            initSyncInternal(initialWait)
        }
    }

    private fun initSyncInternal(initialWait: Int) {
        if (!isSyncQueued) {
            isSyncQueued = true
            syncHandler.postDelayed(syncTask, initialWait.toLong())
        }
    }

    @Synchronized
    fun stopAutomaticSync() {
        stopAutomaticSyncInternal()
    }

    private fun stopAutomaticSyncInternal() {
        syncHandler.removeCallbacks(syncTask)
        isSyncQueued = false
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

    fun syncImmediatelyIfAutomaticSyncEnabled() {
        if (syncConfig.isAutomatic()) {
            syncImmediately()
        }
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
                    tryLogin()
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

                val harvestAndObservationYears = getHarvestAndObservationYears(userInfoJson)

                syncAll(username, harvestAndObservationYears.first, harvestAndObservationYears.second)
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

    @Throws(JSONException::class)
    private suspend fun getHarvestAndObservationYears(userInfoJson: JSONObject): Pair<List<Int>, List<Int>> {
        val harvestYears = filterHarvestYears(
                findHuntingYearsOfLocalHarvests(), parseYears(userInfoJson.getJSONArray("harvestYears")))
        val observationYears = parseYears(userInfoJson.getJSONArray("observationYears"))

        return Pair(harvestYears, observationYears)
    }

    // Use IO dispatcher for database access.
    private suspend fun findHuntingYearsOfLocalHarvests(): List<Int> = withContext(IO) { harvestDatabase.huntingYearsOfHarvests }

    private fun filterHarvestYears(localYears: List<Int>, remoteYears: List<Int>): List<Int> {
        val combinedYears: Set<Int> = localYears union remoteYears
        val filteredYears = ArrayList<Int>(combinedYears.size)

        for (year in combinedYears) {
            val date: Date? = harvestDatabase.getUpdateTimeForHarvestYear(year)

            if (date == null || !Utils.isRecentTime(date, SYNC_MIN_INTERVAL)) {
                filteredYears.add(year)
            }
        }
        filteredYears.sort()
        return filteredYears
    }

    /**
     * Synchronizes announcements, observations, SRVA events, Metsähallitus Permits, harvests and finally images.
     *
     * @param username - Username of the logged in user
     * @param harvestYears - Years to take into account while syncing harvests
     * @param observationYears - Years to take into account while syncing observations
     */
    private suspend fun syncAll(username: String,
                                harvestYears: List<Int>,
                                observationYears: List<Int>) = withContext(Main) {

        announcementSync.sync {
            observationSync.sync(observationYears) {
                srvaSync.sync {
                    metsaHallitusPermitSync.sync(username, Runnable {
                        harvestSync.sync(harvestYears) { harvestChangeEvents ->

                            harvestEventEmitter.emit(harvestChangeEvents)

                            localImageRemover.removeDeletedImagesLocallyAsync()

                            completeSync()
                        }
                    })
                }
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
                syncHandler.postDelayed(syncTask, SYNC_INTERVAL_SECONDS * 1000.toLong())
            } else {
                isSyncQueued = false
            }

            isSyncRunning = false
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
    private fun tryLogin() {
        if (credentialsStore.isCredentialsSaved() && RiistaSDK.currentUserContext.loginStatus.value !is LoginStatus.LoggedIn) {
            CoroutineScope(Main).launch {
                authenticator.reauthenticate(object : AuthSuccessCallback() {
                    override fun onLoginSuccessful(userInfo: String?) {
                        // Try syncing once again.
                        fetchGameLogYearsAndThenSyncAll(false)
                    }
                })
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
        private const val SYNC_INTERVAL_SECONDS = 300
        private const val SYNC_MIN_INTERVAL = 0.5f

        @Throws(JSONException::class)
        private fun parseYears(jArray: JSONArray): List<Int> {
            val years: MutableList<Int> = ArrayList()

            for (i in 0 until jArray.length()) {
                val year = jArray.getInt(i)
                years.add(year)
            }
            return years
        }
    }
}
