package fi.riista.common.domain.huntingControl

import co.touchlab.stately.collections.IsoMutableList
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface HuntingControlGameWardenProvider: DataFetcher {
    val gameWardens: List<HuntingControlGameWarden>?
}

internal class HuntingControlGameWardenFromDatabaseProvider(
    database: RiistaDatabase,
    private val username: String,
    private val rhyId: OrganizationId,
) : HuntingControlGameWardenProvider {

    private val repository = HuntingControlRepository(database)

    private var _gameWardens = IsoMutableList<HuntingControlGameWarden>()
    override val gameWardens: List<HuntingControlGameWarden>?
        get() {
            return if (_gameWardens.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _gameWardens
            }
        }

    override val loadStatus: Observable<LoadStatus> = Observable(LoadStatus.NotLoaded())

    override suspend fun fetch(refresh: Boolean): Unit = coroutineScope {
        if (!refresh && loadStatus.value.loaded) {
            logger.v { "Data has been already loaded, not refreshing" }
            return@coroutineScope
        }

        val gameWardensFromRepository = repository.getGameWardens(
            username = username,
            rhyId = rhyId,
        )

        _gameWardens.clear()
        _gameWardens.addAll(gameWardensFromRepository)
        loadStatus.set(LoadStatus.Loaded())
    }

    fun clear() {
        _gameWardens.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    companion object {
        private val logger by getLogger(HuntingControlGameWardenFromDatabaseProvider::class)
    }
}
