package fi.riista.mobile.observation

import com.google.firebase.crashlytics.FirebaseCrashlytics
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.observation.ObservationOperationResponse
import fi.riista.common.logging.getLogger
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.riistaSdkHelpers.toCommonObservation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

object ObservationDatabaseMigrationToRiistaSDK {
    fun copyObservations(observationDatabase: ObservationDatabase, copyFinished: () -> Unit) {
        observationDatabase.loadNotCopiedObservations { observations ->
            logger.v { "Found ${observations.size} observations" }
            if (observations.isEmpty()) {
                copyFinished()
                return@loadNotCopiedObservations
            }

            val observationCount = AtomicInteger(observations.size)
            observations.forEach { observation ->
                CoroutineScope(Dispatchers.IO).launch {

                    try {
                        copyObservation(observationDatabase, observation)
                    } catch (e: Exception) {
                        log("Exception occurred while migrating an observation: ${e.message}")
                    }

                    // Notify when all observations are copied
                    val currentCount = observationCount.addAndGet(-1)
                    if (currentCount == 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            copyFinished()
                        }
                    }
                }
            }
        }
    }

    private suspend fun copyObservation(observationDatabase: ObservationDatabase, observation: GameObservation) {
        val observationContext = RiistaSDK.observationContext

        // Reset localId as RiistaSDK will assign it when storing the observation
        val commonObservation = observation.toCommonObservation()?.copy(localId = null)
        if (commonObservation != null) {
            val response = observationContext.saveObservation(commonObservation)
            if (response is ObservationOperationResponse.Success) {
                val savedObservationLocalId = response.observation.localId
                if (savedObservationLocalId != null) {
                    observationDatabase.setCommonLocalId(observation.localId, savedObservationLocalId)
                } else {
                    log("Observation copy localId==null")
                }
            } else {
                log("Copying observation failed. $response")
            }
        } else {
            log("Unable to create CommonObservation from GameObservation localId=${observation.localId}, remoteId=${observation.remoteId}")
        }
    }

    private fun log(message: String) {
        logger.w { message }
        FirebaseCrashlytics.getInstance().recordException(ObservationMergeException(message))
    }

    private val logger by getLogger(ObservationDatabaseMigrationToRiistaSDK::class)

    private class ObservationMergeException(message: String) : RuntimeException(message)

}
