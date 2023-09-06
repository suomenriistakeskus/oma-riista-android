package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.logging.getLogger

interface SrvaEventUpdater {
    suspend fun update(username: String, srvaEvents: List<CommonSrvaEvent>, overwriteNonModified: Boolean)
}

internal class SrvaEventToDatabaseUpdater(
    database: RiistaDatabase,
) : SrvaEventUpdater {
    private val repository = SrvaEventRepository(database)

    override suspend fun update(username: String, srvaEvents: List<CommonSrvaEvent>, overwriteNonModified: Boolean) {
        srvaEvents.forEach { event ->
            val shouldUpsert = shouldWriteToDatabase(
                username = username,
                event = event,
                overwriteNonModified = overwriteNonModified
            )
            if (shouldUpsert) {
                try {
                    repository.upsertSrvaEvent(username = username, srvaEvent = event)
                } catch (e: Exception) {
                    logger.w { "Unable to write event to database" }
                }
            }
        }
    }

    private fun shouldWriteToDatabase(
        username: String,
        event: CommonSrvaEvent,
        overwriteNonModified: Boolean,
    ): Boolean {
        if (event.remoteId != null) {
            val oldEvent = repository.getByRemoteId(username, event.remoteId)
            if (oldEvent != null) {
                return isUpdateNeeded(
                    oldEvent = oldEvent,
                    newEvent = event,
                    overwriteNonModified = overwriteNonModified
                )
            }
        }
        return true
    }

    /**
     * Checks whether [oldEvent] should be replaced with [newEvent].
     *
     * Will determine the resolution based on spec version, srva revisions, modified flag and also
     * [overwriteNonModified] flag.
     */
    private fun isUpdateNeeded(
        oldEvent: CommonSrvaEvent,
        newEvent: CommonSrvaEvent,
        overwriteNonModified: Boolean,
    ): Boolean {
        if (overwriteNonModified && !oldEvent.modified) {
            return true
        }
        if (newEvent.srvaSpecVersion > oldEvent.srvaSpecVersion && !oldEvent.modified) {
            return true
        }
        if (newEvent.revision == null || oldEvent.revision == null) {
            return true
        }
        if (newEvent.revision > oldEvent.revision) {
            return true
        }
        return false
    }

    companion object {
        private val logger by getLogger(SrvaEventToDatabaseUpdater::class)
    }
}
