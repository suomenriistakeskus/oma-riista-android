package fi.riista.mobile.service.harvest

import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.HarvestDbHelper.UpdateType
import fi.riista.mobile.event.HarvestChangeEvent
import fi.riista.mobile.event.HarvestChangeEvent.Companion.deleted
import fi.riista.mobile.event.HarvestChangeEvent.Companion.inserted
import fi.riista.mobile.event.HarvestChangeEvent.Companion.updated
import fi.riista.mobile.event.HarvestChangeListener
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.sync.SyncConfig
import fi.vincit.androidutilslib.context.WorkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarvestPersistenceService @Inject constructor(private val harvestDatabase: HarvestDatabase,
                                                    private val syncConfig: SyncConfig,
                                                    private val postHarvestService: PostHarvestService,
                                                    private val harvestEventEmitter: HarvestEventEmitter) {

    fun localPersistHarvest(harvest: GameHarvest, callback: Runnable) {
        // Start IO coroutine.
        CoroutineScope(IO).launch {
            harvestDatabase.addNewLocallyCreatedHarvest(harvest)

            withContext(Main) {
                harvestEventEmitter.emit(inserted(harvest))
                callback.run()
            }
        }
    }

    fun remotePersistHarvest(workContext: WorkContext, harvest: GameHarvest, callback: Runnable) {
        harvest.mSent = false

        // Start IO coroutine.
        CoroutineScope(IO).launch {

            // First update harvest entity locally.
            harvestDatabase.updateHarvest(harvest, true)

            // Then fetch only locally persisted harvests from local database.
            val localHarvests: List<GameHarvest> = harvestDatabase.unsentHarvests

            withContext(Main) {

                if (syncConfig.isAutomatic()) {

                    // Switch to main thread and finally send unsent harvests to backend.
                    postHarvestService.sendUnsentHarvests(workContext, localHarvests, object : HarvestChangeListener {

                        override fun onHarvestsChanged(harvestSendEvents: Collection<HarvestChangeEvent>) {
                            harvestEventEmitter.emit(harvestSendEvents)
                            callback.run()
                        }
                    })

                } else { // manual sync

                    // Emit event for local persistence.
                    harvestEventEmitter.emit(inserted(harvest))
                    callback.run()
                }
            }
        }
    }

    fun remoteUpdateHarvest(workContext: WorkContext, harvest: GameHarvest, callback: HarvestRemoteUpdateResult) {
        harvest.mSent = false

        if (syncConfig.isAutomatic()) {
            postHarvestService.postHarvest(workContext, harvest, callback)
        } else {
            harvestDatabase.updateHarvest(harvest, false)
            callback.onSuccess()
            harvestEventEmitter.emit(updated(harvest))
        }
    }

    fun remoteDeleteHarvest(workContext: WorkContext, harvest: GameHarvest, callback: HarvestRemoteUpdateResult) {
        harvest.mSent = false
        harvest.mPendingOperation = UpdateType.DELETE

        if (!harvest.mRemote) {
            harvestDatabase.removeHarvestByLocalId(harvest.mLocalId)
            callback.onSuccess()
            harvestEventEmitter.emit(deleted(harvest))

        } else if (syncConfig.isAutomatic()) {
            postHarvestService.removeHarvest(workContext, harvest, callback)

        } else {
            harvestDatabase.updateHarvest(harvest, false)
            callback.onSuccess()
            harvestEventEmitter.emit(deleted(harvest))
        }
    }
}
