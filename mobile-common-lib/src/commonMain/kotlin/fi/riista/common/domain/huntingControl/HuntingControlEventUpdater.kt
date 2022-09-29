package fi.riista.common.domain.huntingControl

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.logging.getLogger

sealed class HuntingControlEventOperationResponse {
    /**
     * The operation succeeded.
     */
    data class Success(val event: HuntingControlEvent): HuntingControlEventOperationResponse()

    /**
     * The operation failed.
     */
    data class Failure(val errorMessage: String?): HuntingControlEventOperationResponse()

    /**
     * The event data was invalid or some other precondition failed.
     */
    object Error: HuntingControlEventOperationResponse()
}

interface HuntingControlEventUpdater {
    suspend fun createHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse
    suspend fun updateHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse
}

internal class HuntingControlEventDatabaseUpdater(
    database: RiistaDatabase,
    private val username: String,
) : HuntingControlEventUpdater {
    private val repository = HuntingControlRepository(database)

    override suspend fun createHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse {
        if (username.isBlank()) {
            return HuntingControlEventOperationResponse.Error
        }
        return try {
            val insertedEvent = repository.createHuntingControlEvent(
                username = username,
                event = event,
            )
            HuntingControlEventOperationResponse.Success(insertedEvent)
        } catch (e: Exception) {
            logger.e { "${e.message}" }
            HuntingControlEventOperationResponse.Failure(e.message)
        }
    }

    override suspend fun updateHuntingControlEvent(event: HuntingControlEventData): HuntingControlEventOperationResponse {
        if (username.isBlank()) {
            return HuntingControlEventOperationResponse.Error
        }
        return try {
            val savedEvent = repository.updateHuntingControlEvent(
                event = event,
            )
            HuntingControlEventOperationResponse.Success(savedEvent)
        } catch (e: Exception) {
            HuntingControlEventOperationResponse.Failure(e.message)
        }
    }

    companion object {
        private val logger by getLogger(HuntingControlEventDatabaseUpdater::class)
    }
}
