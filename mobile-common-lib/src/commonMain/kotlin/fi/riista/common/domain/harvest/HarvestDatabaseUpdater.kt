package fi.riista.common.domain.harvest

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.userInfo.CurrentUserContextProvider

interface HarvestUpdater {
    suspend fun saveHarvest(harvest: CommonHarvest?): HarvestOperationResponse
}

internal class HarvestDatabaseUpdater(
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : HarvestUpdater {
    private val repository = HarvestRepository(database)

    override suspend fun saveHarvest(harvest: CommonHarvest?): HarvestOperationResponse {
        val username = currentUserContextProvider.userContext.username
        if (username.isNullOrBlank()) {
            return HarvestOperationResponse.Error("invalid username")
        }
        if (harvest == null) {
            return HarvestOperationResponse.Error("harvest == null")
        }

        return try {
            val insertedHarvest = repository.upsertHarvest(
                username = username,
                harvest = harvest,
            )
            HarvestOperationResponse.Success(insertedHarvest)
        } catch (e: Exception) {
            HarvestOperationResponse.SaveFailure(e.message)
        }
    }
}
