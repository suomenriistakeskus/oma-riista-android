package fi.riista.common.helpers

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.constants.SpeciesCode

class TestSpeciesResolver: SpeciesResolver {

    override fun getSpeciesName(speciesCode: SpeciesCode): String {
        return "SPECIES_$speciesCode"
    }

    override fun getMultipleSpecimensAllowedOnHarvests(speciesCode: SpeciesCode): Boolean {
        return speciesAllowingMultipleSpecimen.contains(speciesCode)
    }

    companion object {
        val INSTANCE = TestSpeciesResolver()

        // taken from species.json
        internal val speciesAllowingMultipleSpecimen = setOf(
            27048, 26298, 26291, 26287, 26373, 26366, 26360, 26382, 26388, 26394, 26407, 26415, 26419, 26427,
            26435, 26440, 26442, 26921, 26922, 26931, 26926, 26928, 27152, 27381, 27649, 27911, 50114, 46564,
            47180, 37178, 37166, 37122, 37142, 27750, 27759, 200535, 33117,
        )
    }
}