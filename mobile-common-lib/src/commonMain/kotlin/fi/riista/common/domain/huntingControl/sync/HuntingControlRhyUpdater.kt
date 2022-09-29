package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.sync.model.LoadRhyHuntingControlEvents
import fi.riista.common.logging.getLogger

sealed class HuntingControlRhyOperationResponse {
    object Success: HuntingControlRhyOperationResponse()
    data class Failure(val errorMessage: String?): HuntingControlRhyOperationResponse()
}

interface HuntingControlRhyUpdater {
    suspend fun update(rhysAndEvents: List<LoadRhyHuntingControlEvents>): HuntingControlRhyOperationResponse
}

internal class HuntingControlRhyToDatabaseUpdater(
    database: RiistaDatabase,
    private val username: String,
) : HuntingControlRhyUpdater {

    private val repository = HuntingControlRepository(database)

    override suspend fun update(rhysAndEvents: List<LoadRhyHuntingControlEvents>): HuntingControlRhyOperationResponse {

        // First update RHYs
        val rhys = rhysAndEvents.map { rhyEvents ->
            rhyEvents.rhy
        }
        try {
            repository.updateRhys(username, rhys)
        } catch (e: Exception) {
            logger.w { "Exception while handling RHYs ${e.message}" }
            return HuntingControlRhyOperationResponse.Failure(e.message)
        }

        // Update game wardens
        val gameWardens = rhysAndEvents.associate { rhyAndEvents ->
            rhyAndEvents.rhy.id to rhyAndEvents.gameWardens
        }
        try {
            repository.updateGameWardens(username, gameWardens)
        } catch (e: Exception) {
            logger.w { "Exception while handling game wardens ${e.message}" }
            return HuntingControlRhyOperationResponse.Failure(e.message)
        }

        // Finally update events
        val events = rhysAndEvents.associate { rhysAndEvents ->
            rhysAndEvents.rhy.id to rhysAndEvents.events
        }
        try {
            repository.updateHuntingControlEvents(username, events)
        } catch (e: Exception) {
            logger.w { "Exception while handling events ${e.message}" }
            return HuntingControlRhyOperationResponse.Failure(e.message)
        }

        return HuntingControlRhyOperationResponse.Success
    }

    companion object {
        private val logger by getLogger(HuntingControlRhyUpdater::class)
    }
}
