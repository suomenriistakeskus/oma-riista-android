package fi.riista.common.domain.huntingControl

import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface HuntingControlRhyProvider: DataFetcher {
    val rhys: List<Organization>?
    val rhyContexts: Map<OrganizationId, HuntingControlRhyContext>?
}

internal class HuntingControlRhyFromDatabaseProvider(
    database: RiistaDatabase,
) : HuntingControlRhyProvider {

    private val repository = HuntingControlRepository(database)
    private val forceRefresh = AtomicBoolean(false)

    private var _rhys = IsoMutableList<Organization>()
    override val rhys: List<Organization>?
        get() {
            return if (_rhys.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _rhys
            }
        }

    private var _rhyContexts = IsoMutableMap<OrganizationId, HuntingControlRhyContext>()
    override val rhyContexts: Map<OrganizationId, HuntingControlRhyContext>?
        get() {
            return if (_rhyContexts.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _rhyContexts
            }
        }

    override val loadStatus: Observable<LoadStatus> = Observable(LoadStatus.NotLoaded())

    override suspend fun fetch(refresh: Boolean): Unit = coroutineScope {
        val shouldRefresh = refresh || forceRefresh.value
        if (!shouldRefresh && loadStatus.value.loaded) {
            logger.v { "Data has been already loaded, not refreshing" }
            return@coroutineScope
        }

        forceRefresh.value = false

        loadStatus.set(LoadStatus.Loading())
        val username = RiistaSDK.INSTANCE.currentUserContextProvider.userContext.username
        if (username == null) {
            logger.w { "No logged in user, can't fetch data." }
            loadStatus.set(LoadStatus.LoadError())
            return@coroutineScope
        }
        val rhysFromDatabase = repository.getRhys(
            username = username,
        )
        val rhyContexts = rhysFromDatabase.map { rhy ->
            HuntingControlRhyContext(rhyId = rhy.id, username = username)
        }.associateBy {
            it.rhyId
        }

        rhyContexts.values.forEach { rhyContext ->
            rhyContext.fetchAllData(refresh = shouldRefresh)
        }

        _rhys.clear()
        _rhys.addAll(rhysFromDatabase)
        _rhyContexts.clear()
        _rhyContexts.putAll(rhyContexts)
        loadStatus.set(LoadStatus.Loaded())
    }

    fun clear() {
        _rhys.clear()
        _rhyContexts.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    fun forceRefreshOnNextFetch() {
        forceRefresh.value = true
    }

    companion object {
        private val logger by getLogger(HuntingControlRhyFromDatabaseProvider::class)
    }
}
