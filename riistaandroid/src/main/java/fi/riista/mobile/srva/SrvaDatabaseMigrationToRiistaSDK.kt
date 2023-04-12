package fi.riista.mobile.srva

import com.google.firebase.crashlytics.FirebaseCrashlytics
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.srva.SrvaEventOperationResponse
import fi.riista.common.logging.getLogger
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.riistaSdkHelpers.toCommonSrvaEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

object SrvaDatabaseMigrationToRiistaSDK {

    fun copyEvents(copyFinished: () -> Unit) {
        SrvaDatabase.getInstance().loadNotCopiedEvents { events ->
            logger.v { "Found ${events.size} events" }
            if (events.isEmpty()) {
                copyFinished()
                return@loadNotCopiedEvents
            }

            val eventCount = AtomicInteger(events.size)
            events.forEach { event ->
                CoroutineScope(Dispatchers.IO).launch {

                    try {
                        copyEvent(event)
                    } catch (e: Exception) {
                        log("Exception occurred while migrating a SRVA event: ${e.message}")
                    }

                    // Notify when all events are copied
                    val currentCount = eventCount.addAndGet(-1)
                    if (currentCount == 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            copyFinished()
                        }
                    }
                }
            }
        }
    }

    private suspend fun copyEvent(event: SrvaEvent) {
        val srvaContext = RiistaSDK.srvaContext

        // Reset localId as RiistaSDK will assign it when storing the event
        val commonSrvaEvent = event.toCommonSrvaEvent()?.copy(localId = null)
        if (commonSrvaEvent != null) {
            val response = srvaContext.saveSrvaEvent(commonSrvaEvent)
            if (response is SrvaEventOperationResponse.Success) {
                val savedEventLocalId = response.srvaEvent.localId
                if (savedEventLocalId != null) {
                    SrvaDatabase.getInstance().setCommonLocalId(event.localId, savedEventLocalId)
                } else {
                    log("Srva event copy localId==null")
                }
            } else {
                log("Copying event failed. $response")
            }
        } else {
            log("Unable to create CommonSrvaEvent from SRVA event localId=${event.localId}, remoteId=${event.remoteId}")
        }
    }

    private fun log(message: String) {
        logger.w { message }
        FirebaseCrashlytics.getInstance().recordException(SrvaMergeException(message))
    }

    private val logger by getLogger(SrvaDatabaseMigrationToRiistaSDK::class)

    private class SrvaMergeException(message: String) : RuntimeException(message)
}
