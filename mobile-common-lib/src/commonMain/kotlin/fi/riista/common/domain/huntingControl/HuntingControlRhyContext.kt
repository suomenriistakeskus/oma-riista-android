package fi.riista.common.domain.huntingControl

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.model.IdentifiesHuntingControlEvent
import fi.riista.common.domain.model.OrganizationId

class HuntingControlRhyContext internal constructor(
    val rhyId: OrganizationId,
    private val username: String,
) {
    private val repository = HuntingControlRepository(RiistaSDK.INSTANCE.database)

    private val huntingControlEventUpdater = HuntingControlEventDatabaseUpdater(
        database = RiistaSDK.INSTANCE.database,
        username = username,
    )

    suspend fun fetchHuntingControlEvents(): List<HuntingControlEvent> {
        return repository.getHuntingControlEvents(
            username = username,
            rhyId = rhyId,
        )
    }

    fun fetchGameWardens(): List<HuntingControlGameWarden> {
        return repository.getGameWardens(
            username = username,
            rhyId = rhyId,
        )
    }

    suspend fun findHuntingControlEvent(identifiesHuntingControlEvent: IdentifiesHuntingControlEvent)
        = repository.getHuntingControlEventByLocalId(identifiesHuntingControlEvent.eventId)

    suspend fun saveHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse {
        return huntingControlEventUpdater.saveHuntingControlEvent(event)
    }
}
