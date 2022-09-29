package fi.riista.mobile.srva

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.network.SyncDataPiece
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SrvaParametersHelper private constructor() {
    val metadata: SrvaMetadata
        get() = RiistaSDK.metadataProvider.srvaMetadata

    val speciesCodes: List<SpeciesCode>
        get() = metadata.species.map { it.speciesCode }

    fun fetchParameters() {
        MainScope().launch {
            RiistaSDK.synchronizeDataPieces(listOf(SyncDataPiece.SRVA_METADATA))
        }
    }

    companion object {
        @JvmStatic
        val instance: SrvaParametersHelper by lazy {
            SrvaParametersHelper()
        }
    }
}