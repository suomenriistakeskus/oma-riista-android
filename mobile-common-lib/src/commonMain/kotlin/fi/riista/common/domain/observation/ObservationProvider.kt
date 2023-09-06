package fi.riista.common.domain.observation

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.isEmpty
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface ObservationProvider: DataFetcher {
    val observations: List<CommonObservation>?
    fun getByLocalId(localId: Long): CommonObservation?
    fun getObservationsHavingImages(): List<CommonObservation>
}

internal class ObservationFromDatabaseProvider(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
): ObservationProvider {

    private val repository = ObservationRepository(database)
    private val forceRefresh = AtomicBoolean(false)

    private var _observations = mutableListOf<CommonObservation>()
    override val observations: List<CommonObservation>?
        get() {
            return if (_observations.isEmpty() && !loadStatus.value.loaded) {
                null
            } else {
                _observations
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
        val observationsFromDatabase = repository.listObservations(
            username = username,
        )

        _observations.clear()
        _observations.addAll(observationsFromDatabase)
        loadStatus.set(LoadStatus.Loaded())
    }

    override fun getByLocalId(localId: Long) = observations?.firstOrNull { it.localId == localId }

    override fun getObservationsHavingImages() = observations
        ?.filter { observation -> observation.images.isEmpty().not() }
        ?: emptyList()

    fun clear() {
        _observations.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    fun forceRefreshOnNextFetch() {
        forceRefresh.value = true
    }

    companion object {
        private val logger by getLogger(ObservationFromDatabaseProvider::class)
    }
}
