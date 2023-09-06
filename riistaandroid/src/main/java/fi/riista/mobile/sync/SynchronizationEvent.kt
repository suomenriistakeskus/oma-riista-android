package fi.riista.mobile.sync

import fi.riista.common.network.sync.SynchronizationLevel

sealed class SynchronizationEvent {
    data class Started(val synchronizationLevel: SynchronizationLevel): SynchronizationEvent()
    data class Completed(val success: Boolean, val synchronizationLevel: SynchronizationLevel): SynchronizationEvent()
}
