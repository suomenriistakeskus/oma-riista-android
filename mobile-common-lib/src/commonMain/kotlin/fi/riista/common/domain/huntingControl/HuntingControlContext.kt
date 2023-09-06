package fi.riista.common.domain.huntingControl

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.dto.toHuntingControlHunterInfo
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.IdentifiesRhy
import fi.riista.common.domain.huntingControl.sync.HuntingControlSynchronizationContext
import fi.riista.common.domain.huntingControl.ui.HuntingControlHunterInfoResponse
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.LoginStatus
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.delegated
import fi.riista.common.preferences.Preferences
import fi.riista.common.reactive.Observable
import fi.riista.common.util.LocalDateTimeProvider

class HuntingControlContext internal constructor(
    backendApiProvider: BackendApiProvider,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : BackendApiProvider by backendApiProvider {

    private val synchronizationContext = HuntingControlSynchronizationContext(
        backendApiProvider = backendApiProvider,
        database = RiistaSDK.INSTANCE.database,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
        commonFileProvider = commonFileProvider,
        currentUserContextProvider = currentUserContextProvider,
    ).delegated(onSyncFinished = ::syncFinished)


    /**
     * Is the hunting control available? Being available indicates that the current user is
     * hunting controller for at least one RHY.
     */
    val huntingControlAvailable = Observable(false)

    private val synchronizeHuntingControlWhenCheckingAvailability = AtomicBoolean(true)
    private val repository = HuntingControlRepository(RiistaSDK.INSTANCE.database)

    fun initialize() {
        RiistaSDK.registerSynchronizationContext(synchronizationContext)
        clearWhenUserLoggedOut()
    }

    fun findRhyContext(identifiesRhy: IdentifiesRhy): HuntingControlRhyContext? {
        val username = currentUserContextProvider.userContext.username ?: return null

        return findRhy(rhyId = identifiesRhy.rhyId)?.let { rhy ->
            HuntingControlRhyContext(rhyId = rhy.id, username = username)
        }
    }

    fun fetchRhys(): List<Organization>? {
        return currentUserContextProvider.userContext.username?.let { username ->
            repository.getRhys(username)
        }
    }

    fun findRhy(rhyId: OrganizationId): Organization? {
        return currentUserContextProvider.userContext.username?.let { username ->
            repository.getRhy(username = username, rhyId = rhyId)
        }
    }

    suspend fun checkAvailability() {
        // todo: should not clear the flag if synchronization fails
        if (synchronizeHuntingControlWhenCheckingAvailability.compareAndSet(expected = true, new = false)) {
            RiistaSDK.synchronize(SyncDataPiece.HUNTING_CONTROL)
        }
    }

    suspend fun fetchHunterInfoByHunterNumber(hunterNumber: HunterNumber): HuntingControlHunterInfoResponse {
        val response = backendAPI.fetchHuntingControlHunterInfoByHunterNumber(hunterNumber)
        response.onSuccess { _, data ->
            return HuntingControlHunterInfoResponse.Success(data.typed.toHuntingControlHunterInfo())
        }
        response.onError { statusCode, exception ->
            logger.w { "Failed to fetch Hunter data $statusCode ${exception?.message}" }
            return when (statusCode) {
                404 -> HuntingControlHunterInfoResponse.Error(HuntingControlHunterInfoResponse.ErrorReason.NOT_FOUND)
                else -> HuntingControlHunterInfoResponse.Error(HuntingControlHunterInfoResponse.ErrorReason.NETWORK_ERROR)
            }
        }
        throw RuntimeException("Unhandled response when fetching hunter info")
    }

    suspend fun fetchHunterInfoBySsn(ssn: String): HuntingControlHunterInfoResponse {
        val response = backendAPI.fetchHuntingControlHunterInfoBySsn(ssn)
        response.onSuccess { _, data ->
            return HuntingControlHunterInfoResponse.Success(data.typed.toHuntingControlHunterInfo())
        }
        response.onError { statusCode, exception ->
            logger.w { "Failed to fetch Hunter data $statusCode ${exception?.message}" }
            return when (statusCode) {
                404 -> HuntingControlHunterInfoResponse.Error(HuntingControlHunterInfoResponse.ErrorReason.NOT_FOUND)
                else -> HuntingControlHunterInfoResponse.Error(HuntingControlHunterInfoResponse.ErrorReason.NETWORK_ERROR)
            }
        }
        throw RuntimeException("Unhandled response when fetching hunter info")
    }

    internal suspend fun sendHuntingControlEventToBackend(event: HuntingControlEvent): Boolean {
        return synchronizationContext.childContext.sendHuntingControlEventToBackend(event)
    }

    private fun syncFinished() {
        val hasRhys = currentUserContextProvider.userContext.username?.let { username ->
            repository.hasRhys(username)
        } ?: false
        huntingControlAvailable.set(hasRhys)
    }

    private fun clearWhenUserLoggedOut() {
        currentUserContextProvider.userContext.loginStatus.bindAndNotify {  loginStatus ->
            if (loginStatus is LoginStatus.NotLoggedIn) {
                clear()
            }
        }
    }

    fun clear() {
        synchronizeHuntingControlWhenCheckingAvailability.value = true
    }

    companion object {
        private val logger by getLogger(HuntingControlContext::class)
    }
}
