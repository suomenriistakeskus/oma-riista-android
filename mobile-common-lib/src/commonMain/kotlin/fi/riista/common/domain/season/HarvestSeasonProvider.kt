package fi.riista.common.domain.season

import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.model.SpeciesCode

interface HarvestSeasonProvider {
    fun getSpeciesHavingSeasons(): List<SpeciesCode>

    fun getHuntingSeasons(speciesCode: SpeciesCode): List<HuntingSeason>
}