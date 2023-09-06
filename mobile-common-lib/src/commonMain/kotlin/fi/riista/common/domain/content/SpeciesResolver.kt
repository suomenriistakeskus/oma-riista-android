package fi.riista.common.domain.content

import fi.riista.common.domain.constants.SpeciesCode

interface SpeciesResolver {
    fun getSpeciesName(speciesCode: SpeciesCode): String?
    fun getMultipleSpecimensAllowedOnHarvests(speciesCode: SpeciesCode): Boolean
}