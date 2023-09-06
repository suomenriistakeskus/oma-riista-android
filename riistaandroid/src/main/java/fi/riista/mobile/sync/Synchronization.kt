package fi.riista.mobile.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.logging.getLogger
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.network.sync.SynchronizationLevel
import fi.riista.common.network.sync.SynchronizedContent
import fi.riista.mobile.AppConfig
import fi.riista.mobile.database.PermitManager
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.network.SynchronizedCookieStore
import fi.vincit.androidutilslib.task.TextTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class Synchronization private constructor(
    private val flags: Set<SynchronizationFlag>,
    private val syncMode: SyncMode,
    private val syncModePreconditionsMet: Boolean,
    private val syncWorkContext: WorkContext,
    private val cookieStore: SynchronizedCookieStore,
    private val announcementSync: AnnouncementSync,
    private val localImageRemover: LocalImageRemover,
    private val permitManager: PermitManager,
    private val listener: Listener,
) {

    interface Listener {
        fun onSynchronizationEvent(event: SynchronizationEvent)
        fun onSynchronizationNetworkError(httpStatusCode: Int)
    }

    val synchronizationLevel: SynchronizationLevel by lazy {
        when {
            syncMode == SyncMode.SYNC_AUTOMATIC && syncModePreconditionsMet -> SynchronizationLevel.USER_CONTENT
            flags.contains(SynchronizationFlag.FORCE_USER_CONTENT_SYNC) -> SynchronizationLevel.USER_CONTENT
            else -> SynchronizationLevel.METADATA
        }
    }

    /**
     * Starts the synchronization.
     */
    fun start() {
        listener.onSynchronizationEvent(SynchronizationEvent.Started(synchronizationLevel))

        fetchUserAccount(notifyAboutNetworkErrors = true)
    }

    /**
     * Resumes the sync after an network error has occurred
     */
    fun resumeAfterNetworkError() {
        // don't report further errors as those most likely cannot be recovered from
        fetchUserAccount(notifyAboutNetworkErrors = false)
    }

    /**
     * Cancels the sync and completes it prematurely.
     */
    fun cancel() {
        completeSynchronization(success = false)
    }

    /**
     * Attempts to fetch user account information.
     *
     * @param   notifyAboutNetworkErrors    Should [listener] be notified about network errors? Consider settings this
     *                                      `true` for the first start call. In that case e.g. 401 error will be
     *                                      reported thus allowing attempting re-login + resuming after network error.
     */
    private fun fetchUserAccount(notifyAboutNetworkErrors: Boolean) {
        // fetch the user account as the very first thing to do. Currently this step is required in order to make
        // sure that the user has logged in and optionally attempt login if there's no valid session token.
        //
        // TODO: consider refactoring RiistaCommon synchronization so that this extra step is not needed
        // - either synchronization functionality should be able to return information telling about 401 or
        // - there should be a separate step ensuring authenticated user before other synchronization
        //   operations are attempted
        //
        // IMPORTANT: FetchUserAccountTask and possibly called tryLogin have side effects
        // - FetchUserAccountTask populates application side cookieStore.
        // - tryLogin updates credentialstore and may navigate to login screen
        //
        // Take these side effects into account if/when refactoring the functionality here
        val fetchUserAccountTask: FetchUserAccountTask = object : FetchUserAccountTask(syncWorkContext, cookieStore) {

            override fun onFinishText(userInfoText: String) {
                handleFetchUserInfoResponseAndContinueSync(userInfoText)
            }

            override fun onError() {
                if (httpStatusCode != -1 && notifyAboutNetworkErrors) {
                    listener.onSynchronizationNetworkError(httpStatusCode)
                } else {
                    logger.d { "Fetching user account failed and not allowed to notify anymore" }
                    completeSynchronization(success = false)
                }
            }
        }
        fetchUserAccountTask.start()
    }

    private fun handleFetchUserInfoResponseAndContinueSync(userInfoText: String) = CoroutineScope(Dispatchers.Main).launch {
        // Originally, it is intended that  pre-loading takes place right after user account info is loaded.
        // Hence, this is currently done here.
        preloadPermits()

        try {
            // Continue processing in a coroutine dispatcher optimized for CPU intensive work.
            withContext(Dispatchers.Default) {
                val userInfoJson = JSONObject(userInfoText)
                val username = userInfoJson.getString("username")

                continueSynchronization(username)
            }
        } catch (e: JSONException) {
            logger.w { "Error while parsing user info JSON: ${e.message}" }
            completeSynchronization(success = false)
        } catch (e: Exception) {
            logger.w { "Error occurred while doing app sync: $e" }
            completeSynchronization(success = false)
        }
    }

    /**
     * Synchronizes announcements, observations, SRVA events, Metsähallitus Permits, harvests and finally images.
     *
     * @param username - Username of the logged in user
     */
    private suspend fun continueSynchronization(username: String) = withContext(Dispatchers.Main) {
        RiistaSDK.synchronize(
            synchronizedContent = SynchronizedContent.SelectedLevel(
                synchronizationLevel = synchronizationLevel
            ),
            config = SynchronizationConfig(
                forceContentReload = flags.contains(SynchronizationFlag.FORCE_CONTENT_RELOAD)
            )
        )

        localImageRemover.removeDeletedImagesLocallyAsync()

        announcementSync.sync {
            completeSynchronization(success = true)
        }
    }

    private fun completeSynchronization(success: Boolean) {
        logger.v { "Synchronization completed (success = $success), notifying.." }
        listener.onSynchronizationEvent(SynchronizationEvent.Completed(success, synchronizationLevel))
    }

    // Preload permits in IO thread.
    private suspend fun preloadPermits() = withContext(Dispatchers.IO) { permitManager.preloadPermits() }


    private open class FetchUserAccountTask(
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
        private val logger by getLogger(Synchronization::class)

        fun create(
            flags: Set<SynchronizationFlag>,
            syncMode: SyncMode,
            syncModePreconditionsMet: Boolean,
            syncWorkContext: WorkContext,
            cookieStore: SynchronizedCookieStore,
            announcementSync: AnnouncementSync,
            localImageRemover: LocalImageRemover,
            permitManager: PermitManager,
            listener: Listener,
        ) = Synchronization(
            flags = flags.toSet(), // make a copy so that original one can be altered (if mutable)
            syncMode = syncMode,
            syncModePreconditionsMet = syncModePreconditionsMet,
            syncWorkContext = syncWorkContext,
            cookieStore = cookieStore,
            announcementSync = announcementSync,
            localImageRemover = localImageRemover,
            permitManager = permitManager,
            listener = listener,
        )
    }
}
