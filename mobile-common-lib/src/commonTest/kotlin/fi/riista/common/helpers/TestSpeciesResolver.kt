package fi.riista.common.helpers

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.constants.SpeciesCode

class TestSpeciesResolver: SpeciesResolver {

    override fun getSpeciesName(speciesCode: SpeciesCode): String {
        return "SPECIES_$speciesCode"
    }

    companion object {
        val INSTANCE = TestSpeciesResolver()
    }
}