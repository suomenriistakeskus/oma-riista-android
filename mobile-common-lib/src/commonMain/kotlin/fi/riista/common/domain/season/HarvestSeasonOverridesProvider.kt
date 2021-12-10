package fi.riista.common.domain.season

import co.touchlab.stately.collections.IsoMutableMap
import fi.riista.common.domain.season.dto.SpeciesHuntingSeasonsDTO
import fi.riista.common.domain.season.dto.toHuntingSeason
import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.logging.getLogger
import fi.riista.common.model.SpeciesCode
import fi.riista.common.util.deserializeFromJson

class HarvestSeasonOverridesProvider : HarvestSeasonProvider {
    private val seasonOverrides = IsoMutableMap<SpeciesCode, List<HuntingSeason>>()

    fun setHuntingSeasons(speciesCode: SpeciesCode,
                          huntingSeasons: List<HuntingSeason>) {
        logger.v { "Overriding seasons for species $speciesCode" }
        seasonOverrides[speciesCode] = huntingSeasons
    }

    fun parseOverridesFromJson(overridesJson: String) {
        overridesJson.deserializeFromJson<List<SpeciesHuntingSeasonsDTO>>()
            ?.forEach { speciesHuntingSeasons ->
                setHuntingSeasons(
                        speciesCode = speciesHuntingSeasons.speciesCode,
                        huntingSeasons = speciesHuntingSeasons.huntingSeasons.map {
                            it.toHuntingSeason()
                        }
                )
            }
    }

    override fun getSpeciesHavingSeasons(): List<SpeciesCode> {
        return seasonOverrides.keys.toList()
    }

    override fun getHuntingSeasons(speciesCode: SpeciesCode): List<HuntingSeason> {
        return seasonOverrides[speciesCode] ?: listOf()
    }

    companion object {
        private val logger by getLogger(HarvestSeasonOverridesProvider::class)
    }
}
