package fi.riista.mobile.database

import com.google.firebase.crashlytics.FirebaseCrashlytics
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.logging.getLogger
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.riistaSdkHelpers.toCommonHarvest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

object HarvestDatabaseMigrationToRiistaSDK {
    fun copyHarvests(harvestDatabase: HarvestDatabase, copyFinished: () -> Unit) {
        val harvests = harvestDatabase.loadNotCopiedHarvests()
        logger.v { "Found ${harvests.size} harvests" }
        if (harvests.isEmpty()) {
            copyFinished()
            return
        }

        val harvestCount = AtomicInteger(harvests.size)
        harvests.forEach { harvest ->
            CoroutineScope(Dispatchers.IO).launch {

                try {
                    copyHarvest(harvestDatabase, harvest)
                } catch (e: Exception) {
                    log("Exception occurred while migrating a harvest: ${e.message}")
                }

                // Notify when all harvests are copied
                val currentCount = harvestCount.addAndGet(-1)
                if (currentCount == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        copyFinished()
                    }
                }
            }
        }
    }

    private suspend fun copyHarvest(harvestDatabase: HarvestDatabase, harvest: GameHarvest) {
        val harvestContext = RiistaSDK.harvestContext

        // Reset localId as RiistaSDK will assign it when storing the harvest
        val commonHarvest = harvest.toCommonHarvest()?.copy(localId = null)
        if (commonHarvest != null) {
            val response = harvestContext.saveHarvest(commonHarvest)
            if (response is HarvestOperationResponse.Success) {
                val savedObservationLocalId = response.harvest.localId
                if (savedObservationLocalId != null) {
                    harvestDatabase.setCommonLocalId(harvest.mLocalId, savedObservationLocalId)
                } else {
                    log("harvest copy localId==null")
                }
            } else {
                log("Copying harvest failed. $response")
            }
        } else {
            log("Unable to create CommonHarvest from GameHarvest localId=${harvest.mLocalId}, remoteId=${harvest.mId}")
        }
    }

    private fun log(message: String) {
        logger.w { message }
        FirebaseCrashlytics.getInstance().recordException(HarvestMergeException(message))
    }

    private val logger by getLogger(HarvestDatabaseMigrationToRiistaSDK::class)

    private class HarvestMergeException(message: String) : RuntimeException(message)
}
