package fi.riista.common.domain.srva

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.isEmpty
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface SrvaEventProvider: DataFetcher {
    val srvaEvents: List<CommonSrvaEvent>?
    fun getByLocalId(localId: Long): CommonSrvaEvent?
    fun getEventsHavingImages(): List<CommonSrvaEvent>
}

internal class SrvaEventFromDatabaseProvider(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
): SrvaEventProvider {

    private val repository = SrvaEventRepository(database)
    private val forceRefresh = AtomicBoolean(false)

    private var _srvaEvents = mutableListOf<CommonSrvaEvent>()
    override val srvaEvents: List<CommonSrvaEvent>?
        get() {
            return if (_srvaEvents.isEmpty() && !loadStatus.value.loaded) {
                null
            } else {
                _srvaEvents
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
        val srvaEventsFromDatabase = repository.listEvents(
            username = username,
        )

        _srvaEvents.clear()
        _srvaEvents.addAll(srvaEventsFromDatabase)
        loadStatus.set(LoadStatus.Loaded())
    }

    override fun getByLocalId(localId: Long): CommonSrvaEvent? {
        return srvaEvents?.firstOrNull { it.localId == localId }
    }

    override fun getEventsHavingImages(): List<CommonSrvaEvent> {
        return srvaEvents
            ?.filter { event -> event.images.isEmpty().not() }
            ?: emptyList()
    }

    fun clear() {
        _srvaEvents.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    fun forceRefreshOnNextFetch() {
        forceRefresh.value = true
    }

    companion object {
        private val logger by getLogger(SrvaEventFromDatabaseProvider::class)
    }
}
