package fi.riista.mobile.service.harvest

import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.HarvestDbHelper.UpdateType
import fi.riista.mobile.event.HarvestChangeEvent
import fi.riista.mobile.event.HarvestChangeEvent.Companion.deleted
import fi.riista.mobile.event.HarvestChangeEvent.Companion.inserted
import fi.riista.mobile.event.HarvestChangeEvent.Companion.updated
import fi.riista.mobile.event.HarvestChangeListener
import fi.riista.mobile.gamelog.HarvestSpecVersionResolver
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.network.PostHarvestTask
import fi.riista.mobile.utils.OperationCounter
import fi.riista.mobile.utils.Utils
import fi.vincit.androidutilslib.context.WorkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostHarvestService @Inject constructor(private val harvestDatabase: HarvestDatabase,
                                             private val harvestEventEmitter: HarvestEventEmitter,
                                             private val specVersionResolver: HarvestSpecVersionResolver) {

    private val localIdsOfHarvestsBeingSent = ConcurrentHashMap<Int, Any>()

    fun postHarvest(workContext: WorkContext, harvest: GameHarvest, callback: HarvestRemoteUpdateResult) {
        val specVersion = specVersionResolver.resolveHarvestSpecVersion()

        val task: PostHarvestTask = object : PostHarvestTask(workContext, harvestDatabase, harvest, specVersion) {

            override fun onHarvestSent() {
                callback.onSuccess()
                harvestEventEmitter.emit(updated(harvest))
            }

            override fun onImageCompletion(errors: Boolean, sentImageUpdates: Int) {
                if (!errors) {
                    harvestEventEmitter.emit(updated(harvest))
                }
            }

            override fun onHarvestWaitingDelivery() {
                callback.onSuccess()
                harvestEventEmitter.emit(inserted(harvest))
            }

            override fun onHarvestOutdated() {
                callback.onOutdated()
            }

            override fun onHarvestSendingFailed() {
                Utils.printTaskInfo("onHarvestSendingFailed()", this)
                callback.onFailure()
            }
        }
        task.start()
    }

    fun removeHarvest(workContext: WorkContext, harvest: GameHarvest, callback: HarvestRemoteUpdateResult) {
        val specVersion = specVersionResolver.resolveHarvestSpecVersion()

        val task: PostHarvestTask = object : PostHarvestTask(workContext, harvestDatabase, harvest, specVersion) {

            override fun onHarvestSent() {
                removeLocalHarvest()
            }

            override fun onHarvestWaitingDelivery() {
                removeLocalHarvest()
            }

            private fun removeLocalHarvest() {
                harvestDatabase.removeHarvestByLocalId(harvest.mLocalId)
                callback.onSuccess()
                harvestEventEmitter.emit(deleted(harvest))
            }

            override fun onHarvestOutdated() {
                callback.onFailure()
            }

            override fun onHarvestSendingFailed() {
                callback.onFailure()
            }
        }
        task.start()
    }

    /**
     * Sends unsent harvests to backend.
     *
     * @param workContext WorkContext to use while initiating network fetch tasks
     * @param harvests Harvest objects to be sent to backend
     * @param callback Listener object that is triggered when all harvest have been sent. Image update
     * events are not included.
     */
    fun sendUnsentHarvests(workContext: WorkContext, harvests: List<GameHarvest>, callback: HarvestChangeListener) {
        if (harvests.isEmpty()) {
            callback.onHarvestsChanged(emptyList())
            return
        }

        val sentHarvests = ConcurrentLinkedQueue<GameHarvest>()

        val harvestChangeEvents = ConcurrentLinkedQueue<HarvestChangeEvent>()
        val imageUpdateEvents = ConcurrentLinkedQueue<HarvestChangeEvent>()

        val numHarvestsToSend = harvests.size
        val processedHarvestCounter = OperationCounter(numHarvestsToSend)
        val processedHarvestImagesCounter = OperationCounter(numHarvestsToSend)

        for (harvest in harvests) {

            // Check if harvest is already being processed by another thread.
            if (localIdsOfHarvestsBeingSent.putIfAbsent(harvest.mLocalId, MARKER) == null) {

                val specVersion = specVersionResolver.resolveHarvestSpecVersion()

                val task: PostHarvestTask = object : PostHarvestTask(workContext, harvestDatabase, harvest, specVersion) {

                    override fun onHarvestSent() {
                        harvestChangeEvents.add(when {
                            isDeletePending(harvest) -> deleted(harvest)
                            else -> updated(harvest)
                        })

                        sentHarvests.add(harvest)
                    }

                    override fun onHarvestWaitingDelivery() {
                        harvestChangeEvents.add(when {
                            isDeletePending(harvest) -> deleted(harvest)
                            else -> updated(harvest)
                        })
                    }

                    override fun onHarvestSendingFailed() {
                        if (!harvest.mRemote) { // is new?
                            harvestChangeEvents.add(when {
                                isDeletePending(harvest) -> deleted(harvest)
                                else -> inserted(harvest)
                            })
                        }
                    }

                    override fun onTextCompletion() {
                        // Indicate that harvest processing is ready.
                        localIdsOfHarvestsBeingSent.remove(harvest.mLocalId)

                        if (processedHarvestCounter.incrementOperationsDone()) {
                            CoroutineScope(Main).launch {
                                // Do pruning removed images in IO coroutine.
                                withContext(IO) { harvestDatabase.pruneHarvestsRemovedOnServer(sentHarvests) }

                                callback.onHarvestsChanged(harvestChangeEvents)
                            }
                        }
                    }

                    override fun onImageCompletion(errors: Boolean, sentImageUpdates: Int) {
                        if (!errors && sentImageUpdates > 0) {
                            imageUpdateEvents.add(updated(harvest))
                        }

                        if (processedHarvestImagesCounter.incrementOperationsDone() && imageUpdateEvents.isNotEmpty()) {
                            harvestEventEmitter.emit(imageUpdateEvents)
                        }
                    }
                }
                task.start()

            } else {
                if (processedHarvestCounter.incrementOperationsDone()) {
                    CoroutineScope(Main).launch {
                        // Do pruning removed images in IO coroutine.
                        withContext(IO) { harvestDatabase.pruneHarvestsRemovedOnServer(sentHarvests) }

                        callback.onHarvestsChanged(harvestChangeEvents)
                    }
                }

                if (processedHarvestImagesCounter.incrementOperationsDone() && imageUpdateEvents.isNotEmpty()) {
                    harvestEventEmitter.emit(imageUpdateEvents)
                }
            }
        }
    }

    companion object {

        // Dummy object used as Map key
        private val MARKER = Any()

        private fun isDeletePending(harvest: GameHarvest): Boolean {
            return harvest.mPendingOperation == UpdateType.DELETE
        }
    }
}
