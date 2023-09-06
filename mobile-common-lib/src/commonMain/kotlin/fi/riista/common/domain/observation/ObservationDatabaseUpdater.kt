package fi.riista.common.domain.observation

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.userInfo.CurrentUserContextProvider

interface ObservationUpdater {
    suspend fun saveObservation(observation: CommonObservation?): ObservationOperationResponse
}

internal class ObservationDatabaseUpdater(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : ObservationUpdater {
    private val repository = ObservationRepository(database)

    override suspend fun saveObservation(observation: CommonObservation?): ObservationOperationResponse {
        val username = currentUserContextProvider.userContext.username
        if (username.isNullOrBlank()) {
            return ObservationOperationResponse.Error("invalid username")
        }
        if (observation == null) {
            return ObservationOperationResponse.Error("observation == null")
        }

        return try {
            val insertedObservation = repository.upsertObservation(
                username = username,
                observation = observation,
            )
            ObservationOperationResponse.Success(insertedObservation)
        } catch (e: Exception) {
            ObservationOperationResponse.SaveFailure(e.message)
        }
    }
}

