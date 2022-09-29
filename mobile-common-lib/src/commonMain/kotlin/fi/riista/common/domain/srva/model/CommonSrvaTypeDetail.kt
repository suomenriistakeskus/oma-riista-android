package fi.riista.common.domain.srva.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaTypeDetail(
    val detailType: BackendEnum<SrvaEventTypeDetail>,

    // applicable for specified species or for all if empty
    val speciesCodes: List<SpeciesCode>,
) {
    internal constructor(detailType: SrvaEventTypeDetail): this(
        detailType = detailType.toBackendEnum(),
        speciesCodes = listOf(),
    )

    fun isApplicableFor(speciesCode: SpeciesCode): Boolean {
        return speciesCodes.isEmpty() || speciesCodes.contains(speciesCode)
    }
}
