package fi.riista.common.domain.season

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.season.model.HuntingSeason

interface HarvestSeasonProvider {
    fun getSpeciesHavingSeasons(): List<SpeciesCode>

    fun getHuntingSeasons(speciesCode: SpeciesCode): List<HuntingSeason>
}