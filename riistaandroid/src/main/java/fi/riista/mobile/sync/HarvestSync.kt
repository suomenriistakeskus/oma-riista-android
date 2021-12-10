package fi.riista.mobile.sync

import android.util.Log
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.event.HarvestChangeEvent
import fi.riista.mobile.event.HarvestChangeListener
import fi.riista.mobile.gamelog.HarvestSpecVersionResolver
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.network.FetchHarvestsTask
import fi.riista.mobile.service.harvest.PostHarvestService
import fi.riista.mobile.utils.Consumer
import fi.vincit.androidutilslib.context.WorkContext
import java.util.Collections.emptyList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

typealias HarvestEventsConsumer = Consumer<Collection<HarvestChangeEvent>>

@Singleton
class HarvestSync @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext,
        private val harvestDatabase: HarvestDatabase,
        private val postHarvestService: PostHarvestService,
        private val specVersionResolver: HarvestSpecVersionResolver) {

    fun sync(harvestYears: List<Int>, emitEvents: HarvestEventsConsumer) {

        sendUnsentHarvests(object : HarvestChangeListener {
            override fun onHarvestsChanged(outgoingEvents: Collection<HarvestChangeEvent>) {

                val cumulatedEvents = outgoingEvents.toMutableList()

                fetchHarvests(harvestYears, cumulatedEvents, emitEvents)
            }
        })
    }

    private fun sendUnsentHarvests(callback: HarvestChangeListener) {
        val unsentHarvests: List<GameHarvest> = harvestDatabase.unsentHarvests

        if (unsentHarvests.isNotEmpty()) {
            postHarvestService.sendUnsentHarvests(syncWorkContext, unsentHarvests, callback)
        } else {
            // No unsent harvests exist in local database.
            callback.onHarvestsChanged(emptyList())
        }
    }

    private fun fetchHarvests(years: List<Int>,
                              allEvents: MutableList<HarvestChangeEvent>,
                              emitEvents: HarvestEventsConsumer) {

        if (years.isEmpty()) {
            emitEvents(allEvents)
        } else {
            val huntingYear = years[0]
            val remainingYears = years.drop(1)
            val specVersion = specVersionResolver.resolveHarvestSpecVersion()

            val harvestTask: FetchHarvestsTask = object : FetchHarvestsTask(syncWorkContext, harvestDatabase, huntingYear, specVersion) {

                override fun onLoad(harvestChanges: List<HarvestChangeEvent>?) {
                    harvestChanges?.let { allEvents.addAll(it) }
                }

                override fun onError() {
                    Log.d(TAG, "Could not sync harvests for hunting year $huntingYear")
                }

                override fun onEnd() {
                    fetchHarvests(remainingYears, allEvents, emitEvents)
                }
            }
            harvestTask.start()
        }
    }

    companion object {
        private const val TAG = "HarvestSync"
    }
}
