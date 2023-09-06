package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.logging.getLogger

interface HarvestUpdater {
    suspend fun update(username: String, harvests: List<CommonHarvest>, overwriteNonModified: Boolean)
}

internal class HarvestToDatabaseUpdater(
    database: RiistaDatabase,
) : HarvestUpdater {
    private val repository = HarvestRepository(database)

    override suspend fun update(username: String, harvests: List<CommonHarvest>, overwriteNonModified: Boolean) {
        harvests.forEach { harvest ->
            val shouldUpsert = shouldWriteToDatabase(
                username = username,
                harvest = harvest,
                overwriteNonModified = overwriteNonModified
            )

            if (shouldUpsert) {
                try {
                    repository.upsertHarvest(username = username, harvest = harvest)
                } catch (e: Exception) {
                    logger.w { "Unable to write harvest to database" }
                }
            }
        }
    }

    private suspend fun shouldWriteToDatabase(
        username: String,
        harvest: CommonHarvest,
        overwriteNonModified: Boolean
    ): Boolean {
        if (harvest.id != null) {
            val oldEvent = repository.getByRemoteId(username, harvest.id)
            if (oldEvent != null) {
                return isUpdateNeeded(
                    oldHarvest = oldEvent,
                    newHarvest = harvest,
                    overwriteNonModified = overwriteNonModified
                )
            }
        }
        return true
    }

    /**
     * Checks whether [oldHarvest] should be replaced with [newHarvest].
     *
     * Will determine the resolution based on spec version, harvest revisions, modified flag and also
     * [overwriteNonModified] flag.
     */
    private fun isUpdateNeeded(
        oldHarvest: CommonHarvest,
        newHarvest: CommonHarvest,
        overwriteNonModified: Boolean,
    ): Boolean {
        if (overwriteNonModified && !oldHarvest.modified) {
            return true
        }
        if (newHarvest.harvestSpecVersion > oldHarvest.harvestSpecVersion && !oldHarvest.modified) {
            return true
        }
        if (newHarvest.rev == null || oldHarvest.rev == null) {
            return true
        }
        if (newHarvest.rev > oldHarvest.rev) {
            return true
        }
        return false
    }

    companion object {
        private val logger by getLogger(HarvestToDatabaseUpdater::class)
    }
}
