package fi.riista.mobile.observation

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.network.SyncDataPiece
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ObservationMetadataHelper private constructor() {
    fun fetchMetadata() {
        MainScope().launch {
            RiistaSDK.synchronizeDataPieces(listOf(SyncDataPiece.OBSERVATION_METADATA))
        }
    }

    companion object {
        @JvmStatic
        val instance: ObservationMetadataHelper by lazy {
            ObservationMetadataHelper()
        }
    }
}