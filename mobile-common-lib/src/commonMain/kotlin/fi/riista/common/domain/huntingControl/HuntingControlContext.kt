package fi.riista.common.domain.huntingControl

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.model.IdentifiesRhy
import fi.riista.common.domain.huntingControl.sync.HuntingControlSynchronizationContextProvider
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.SyncDataPiece
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.coroutines.coroutineScope

class HuntingControlContext internal constructor(
    backendApiProvider: BackendApiProvider,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
) : BackendApiProvider by backendApiProvider {

    private val syncContextProvider = HuntingControlSynchronizationContextProvider(
        backendApiProvider = backendApiProvider,
        database = RiistaSDK.INSTANCE.database,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
        commonFileProvider = commonFileProvider,
        syncFinishedListener = ::syncFinished,
    )

    private val _huntingControlRhyProvider = HuntingControlRhyFromDatabaseProvider(
        RiistaSDK.INSTANCE.database,
    )
    val huntingControlRhyProvider: HuntingControlRhyProvider = _huntingControlRhyProvider

    val huntingControlRhys: List<Organization>?
        get() = huntingControlRhyProvider.rhys

    /**
     * Is the hunting control available? Being available indicates that the current user is
     * hunting controller for at least one RHY.
     */
    val huntingControlAvailable: Boolean
        get() {
            return _huntingControlRhyProvider.rhyContexts?.isNotEmpty() ?: false
        }

    private val synchronizeHuntingControlWhenCheckingAvailability = AtomicBoolean(true)

    init {
        RiistaSDK.registerSyncContextProvider(SyncDataPiece.HUNTING_CONTROL, syncContextProvider)
    }

    fun findRhyContext(identifiesRhy: IdentifiesRhy): HuntingControlRhyContext? {
        return huntingControlRhyProvider.rhyContexts?.get(identifiesRhy.rhyId)
    }

    fun findRhy(rhyId: OrganizationId): Organization? {
        return huntingControlRhys?.firstOrNull { rhy ->
            rhy.id == rhyId
        }
    }

    suspend fun checkAvailability(refresh: Boolean = false) {
        // todo: should not clear the flag if synchronization fails
        if (synchronizeHuntingControlWhenCheckingAvailability.compareAndSet(expected = true, new = false)) {
            RiistaSDK.synchronizeDataPieces(listOf(SyncDataPiece.HUNTING_CONTROL))
        }

        fetchRhys(refresh = refresh)
    }

    suspend fun fetchRhys(refresh: Boolean) = coroutineScope {
        huntingControlRhyProvider.fetch(refresh = refresh)
    }

    private fun syncFinished() {
        _huntingControlRhyProvider.forceRefreshOnNextFetch()
    }

    fun clear() {
        _huntingControlRhyProvider.clear()
        synchronizeHuntingControlWhenCheckingAvailability.value = true
    }
}
