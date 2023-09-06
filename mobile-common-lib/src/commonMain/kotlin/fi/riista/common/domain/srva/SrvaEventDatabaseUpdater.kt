package fi.riista.common.domain.srva

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.userInfo.CurrentUserContextProvider

interface SrvaEventUpdater {
    suspend fun saveSrvaEvent(srvaEvent: CommonSrvaEvent?): SrvaEventOperationResponse
}

internal class SrvaEventDatabaseUpdater(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider
): SrvaEventUpdater {
    private val repository = SrvaEventRepository(database)

    override suspend fun saveSrvaEvent(srvaEvent: CommonSrvaEvent?): SrvaEventOperationResponse {
        val username = currentUserContextProvider.userContext.username
        if (username.isNullOrBlank() || srvaEvent == null) {
            return SrvaEventOperationResponse.Error("invalid username")
        }

        return try {
            val insertedEvent = repository.upsertSrvaEvent(
                username = username,
                srvaEvent = srvaEvent,
            )
            SrvaEventOperationResponse.Success(insertedEvent)
        } catch (e: Exception) {
            SrvaEventOperationResponse.SaveFailure(e.message)
        }
    }
}
