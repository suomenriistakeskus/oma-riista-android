package fi.riista.mobile.service.harvest

import fi.riista.mobile.event.HarvestChangeEvent
import fi.riista.mobile.event.HarvestChangeListener
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarvestEventEmitter @Inject constructor() {

    // Mutating a {@code CopyOnWriteArrayList} is costly but traversals are safe to use between different threads.
    private val listeners: MutableList<HarvestChangeListener> = CopyOnWriteArrayList()

    fun addListener(listener: HarvestChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: HarvestChangeListener) {
        listeners.remove(listener)
    }

    fun emit(events: Collection<HarvestChangeEvent>) {
        listeners.forEach { it.onHarvestsChanged(events) }
    }

    fun emit(event: HarvestChangeEvent) {
        emit(listOf(event))
    }
}
