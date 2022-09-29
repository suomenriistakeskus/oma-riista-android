package fi.riista.common.domain.huntingControl

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.model.IdentifiesHuntingControlEvent
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.logging.getLogger
import kotlinx.coroutines.coroutineScope

class HuntingControlRhyContext internal constructor(
    val rhyId: OrganizationId,
    username: String,
) {
    private val _huntingControlEventProvider = HuntingControlEventFromDatabaseProvider(
        database = RiistaSDK.INSTANCE.database,
        username = username,
        rhyId = rhyId,
    )
    val huntingControlEventProvider: HuntingControlEventProvider = _huntingControlEventProvider

    private val _gameWardenProvider = HuntingControlGameWardenFromDatabaseProvider(
        database = RiistaSDK.INSTANCE.database,
        username = username,
        rhyId = rhyId,
    )
    val gameWardenProvider: HuntingControlGameWardenProvider = _gameWardenProvider

    private val _huntingControlEventUpdater = HuntingControlEventDatabaseUpdater(
        database = RiistaSDK.INSTANCE.database,
        username = username,
    )
    val huntingControlEventUpdater: HuntingControlEventUpdater = _huntingControlEventUpdater

    val huntingControlEvents: List<HuntingControlEvent>?
        get() = huntingControlEventProvider.huntingControlEvents

    val gameWardens: List<HuntingControlGameWarden>?
        get() = gameWardenProvider.gameWardens

    suspend fun fetchAllData(refresh: Boolean) = coroutineScope {
        huntingControlEventProvider.fetch(refresh = refresh)
        gameWardenProvider.fetch(refresh = refresh)
    }

    suspend fun fetchHuntingControlEvent(
        identifiesHuntingControlEvent: IdentifiesHuntingControlEvent,
        allowCached: Boolean,
    ): HuntingControlEvent? {
        if (allowCached) {
            findHuntingControlEvent(identifiesHuntingControlEvent)
                ?.let { event ->
                    return event
                }

            fetchAllData(refresh = false)
        } else {
            fetchAllData(refresh = true)
        }

        return findHuntingControlEvent(identifiesHuntingControlEvent)
    }

    fun findHuntingControlEvent(identifiesHuntingControlEvent: IdentifiesHuntingControlEvent): HuntingControlEvent? {
        return huntingControlEventProvider.huntingControlEvents?.firstOrNull { event ->
            event.localId == identifiesHuntingControlEvent.eventId
        }
    }

    suspend fun createHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse {
        val response = huntingControlEventUpdater.createHuntingControlEvent(event)
        if (response is HuntingControlEventOperationResponse.Success) {
            huntingControlEventProvider.fetch(refresh = true)
        }
        return response
    }

    suspend fun updateHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse {
        val response = huntingControlEventUpdater.updateHuntingControlEvent(event)
        if (response is HuntingControlEventOperationResponse.Success) {
            huntingControlEventProvider.fetch(refresh = true)
        }
        return response
    }

    fun clear() {
        _huntingControlEventProvider.clear()
        _gameWardenProvider.clear()
    }

    companion object {
        private val logger by getLogger(HuntingControlRhyContext::class)
    }
}

