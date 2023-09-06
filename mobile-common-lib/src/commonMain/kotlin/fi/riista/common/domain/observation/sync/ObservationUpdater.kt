package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.logging.getLogger

interface ObservationUpdater {
    suspend fun update(username: String, observations: List<CommonObservation>, overwriteNonModified: Boolean)
}

internal class ObservationToDatabaseUpdater(
    database: RiistaDatabase,
) : ObservationUpdater {
    private val repository = ObservationRepository(database)

    override suspend fun update(username: String, observations: List<CommonObservation>, overwriteNonModified: Boolean) {
        observations.forEach { observation ->
            val shouldUpsert = shouldWriteToDatabase(
                username = username,
                observation = observation,
                overwriteNonModified = overwriteNonModified
            )
            if (shouldUpsert) {
                try {
                    repository.upsertObservation(username = username, observation = observation)
                } catch (e: Exception) {
                    logger.w { "Unable to write event to database" }
                }
            }
        }
    }

    private fun shouldWriteToDatabase(
        username: String,
        observation: CommonObservation,
        overwriteNonModified: Boolean,
    ): Boolean {
        if (observation.remoteId != null) {
            val oldEvent = repository.getByRemoteId(username, observation.remoteId)
            if (oldEvent != null) {
                return isUpdateNeeded(
                    oldObservation = oldEvent,
                    newObservation = observation,
                    overwriteNonModified = overwriteNonModified,
                )
            }
        }
        return true
    }

    /**
     * Checks whether [oldObservation] should be replaced with [newObservation].
     *
     * Will determine the resolution based on spec version, observation revisions, modified flag and also
     * [overwriteNonModified] flag.
     */
    private fun isUpdateNeeded(
        oldObservation: CommonObservation,
        newObservation: CommonObservation,
        overwriteNonModified: Boolean,
    ): Boolean {
        if (overwriteNonModified && !oldObservation.modified) {
            return true
        }
        if (newObservation.observationSpecVersion > oldObservation.observationSpecVersion && !oldObservation.modified) {
            return true
        }
        if (newObservation.revision == null || oldObservation.revision == null) {
            return true
        }
        if (newObservation.revision > oldObservation.revision) {
            return true
        }
        return false
    }

    companion object {
        private val logger by getLogger(ObservationToDatabaseUpdater::class)
    }
}
