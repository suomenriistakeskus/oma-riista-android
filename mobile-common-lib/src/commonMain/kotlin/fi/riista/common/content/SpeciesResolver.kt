package fi.riista.common.content

import fi.riista.common.model.SpeciesCode

interface SpeciesResolver {
    fun getSpeciesName(speciesCode: SpeciesCode): String?
}