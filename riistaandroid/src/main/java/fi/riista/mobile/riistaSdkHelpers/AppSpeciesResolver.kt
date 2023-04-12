package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.mobile.database.SpeciesInformation

class AppSpeciesResolver: SpeciesResolver {
    override fun getSpeciesName(speciesCode: SpeciesCode): String? =
        SpeciesInformation.getSpeciesName(speciesCode)

    override fun getMultipleSpecimensAllowedOnHarvests(speciesCode: SpeciesCode): Boolean =
        SpeciesInformation.getSpecies(speciesCode)?.mMultipleSpecimenAllowedOnHarvests == true
}