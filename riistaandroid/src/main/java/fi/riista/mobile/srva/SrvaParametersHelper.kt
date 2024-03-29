package fi.riista.mobile.srva

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.network.sync.SyncDataPiece
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SrvaParametersHelper private constructor() {
    private val metadata: SrvaMetadata
        get() = RiistaSDK.metadataProvider.srvaMetadata

    val speciesCodes: List<SpeciesCode>
        get() = metadata.species.map { it.speciesCode }

    fun fetchParameters() {
        MainScope().launch {
            RiistaSDK.synchronize(SyncDataPiece.SRVA_METADATA)
        }
    }

    companion object {
        @JvmStatic
        val instance: SrvaParametersHelper by lazy {
            SrvaParametersHelper()
        }
    }
}