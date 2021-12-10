package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.content.SpeciesResolver
import fi.riista.common.model.SpeciesCode
import fi.riista.mobile.database.SpeciesInformation

class AppSpeciesResolver: SpeciesResolver {
    override fun getSpeciesName(speciesCode: SpeciesCode): String? =
        SpeciesInformation.getSpeciesName(speciesCode)
}