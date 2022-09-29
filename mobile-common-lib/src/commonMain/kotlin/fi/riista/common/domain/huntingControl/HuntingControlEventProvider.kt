package fi.riista.common.domain.huntingControl

import co.touchlab.stately.collections.IsoMutableList
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher
import kotlinx.coroutines.coroutineScope

interface HuntingControlEventProvider: DataFetcher {
    val huntingControlEvents: List<HuntingControlEvent>?
}

internal class HuntingControlEventFromDatabaseProvider(
    database: RiistaDatabase,
    private val username: String,
    private val rhyId: OrganizationId,
) : HuntingControlEventProvider {

    private val repository = HuntingControlRepository(database)

    private var _huntingControlEvents = IsoMutableList<HuntingControlEvent>()
    override val huntingControlEvents: List<HuntingControlEvent>?
        get() {
            return if (_huntingControlEvents.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _huntingControlEvents
            }
        }

    override val loadStatus: Observable<LoadStatus> = Observable(LoadStatus.NotLoaded())

    override suspend fun fetch(refresh: Boolean): Unit = coroutineScope {
        if (!refresh && loadStatus.value.loaded) {
            logger.v { "Data has been already loaded, not refreshing" }
            return@coroutineScope
        }

        val eventsFromRepository = repository.getHuntingControlEvents(
            username = username,
            rhyId = rhyId,
        )

        _huntingControlEvents.clear()
        _huntingControlEvents.addAll(eventsFromRepository)
        loadStatus.set(LoadStatus.Loaded())
    }

    fun clear() {
        _huntingControlEvents.clear()
        loadStatus.set(LoadStatus.NotLoaded())
    }

    companion object {
        private val logger by getLogger(HuntingControlEventFromDatabaseProvider::class)
    }
}
