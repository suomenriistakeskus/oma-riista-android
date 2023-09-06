package fi.riista.common.domain.permit.harvestPermit

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.model.Species

@kotlinx.serialization.Serializable
data class CommonHarvestPermit(
    val permitNumber: PermitNumber?,
    val permitType: String?,
    val speciesAmounts: List<CommonHarvestPermitSpeciesAmount>,

    val available: Boolean,
) {
    fun isAvailableForSpecies(species: Species): Boolean {
        return available && getSpeciesAmountFor(species) != null
    }

    fun getSpeciesAmountFor(species: Species): CommonHarvestPermitSpeciesAmount? =
        species.knownSpeciesCodeOrNull()?.let { speciesCode ->
            getSpeciesAmountFor(speciesCode)
        }

    fun getSpeciesAmountFor(speciesCode: SpeciesCode): CommonHarvestPermitSpeciesAmount? {
        return speciesAmounts.firstOrNull { it.speciesCode == speciesCode }
    }
}
