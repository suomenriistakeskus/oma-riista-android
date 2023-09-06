package fi.riista.common.domain.harvest

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.isEmpty
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface HarvestProvider: DataFetcher {
    val harvests: List<CommonHarvest>?
    fun getByLocalId(localId: Long): CommonHarvest?
    fun getHarvestsHavingImages(): List<CommonHarvest>
}

internal class HarvestFromDatabaseProvider(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
): HarvestProvider {

    private val repository = HarvestRepository(database)
    private val forceRefresh = AtomicBoolean(false)

    private var _harvests = mutableListOf<CommonHarvest>()
    override val harvests: List<CommonHarvest>?
        get() {
            return if (_harvests.isEmpty() && !loadStatus.value.loaded) {
                null
            } else {
                _harvests
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
        val username = currentUserContextProvider.userContext.username
        if (username == null) {
            logger.w { "No logged in user, can't fetch data." }
            loadStatus.set(LoadStatus.LoadError())
            return@coroutineScope
        }
        val harvestsFromDatabase = repository.listHarvests(
            username = username,
        )

        _harvests.clear()
        _harvests.addAll(harvestsFromDatabase)
        loadStatus.set(LoadStatus.Loaded())
    }

    override fun getByLocalId(localId: Long) = harvests?.firstOrNull { it.localId == localId }

    override fun getHarvestsHavingImages() = harvests
        ?.filter { it.images.isEmpty().not() }
        ?: emptyList()

    fun clear() {
        _harvests.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    fun forceRefreshOnNextFetch() {
        forceRefresh.value = true
    }

    companion object {
        private val logger by getLogger(HarvestFromDatabaseProvider::class)
    }
}
