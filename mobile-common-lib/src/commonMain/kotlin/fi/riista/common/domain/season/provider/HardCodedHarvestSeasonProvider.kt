package fi.riista.common.domain.season.provider

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HardcodedHarvestSeason
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.model.Date
import fi.riista.common.model.DatePeriod
import fi.riista.common.util.contains

class HardCodedHarvestSeasonProvider : HarvestSeasonProvider {
    private val hardcodedSeasons: Map<SpeciesCode, List<HardcodedHarvestSeason>> = listOf(
        // Metsähanhi
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.BEAN_GOOSE_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(8, 27)),
                DatePeriod(Date(10, 1), Date(11, 30)),
            )
        ),

        // Karhu
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.BEAR_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(10, 31)),
            )
        ),

        // Haahka
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.COMMON_EIDER_ID,
            startYear = 2019, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(6, 1), Date(6, 15)),
            )
        ),

        // Nokikana
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.COOT_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Heinätavi
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.GARGANEY_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Isokoskelo
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.GOOSANDER_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(9, 1), Date(12, 31)),
            )
        ),

        // Merihanhi
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.GREYLAG_GOOSE_ID,
            startYear = 2021, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 10), Date(12, 31)),
            )
        ),

        // Halli
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.GREY_SEAL_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(4, 16), Date(12, 31)),
            )
        ),

        // Alli
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.LONG_TAILED_DUCK_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(9, 1), Date(12, 31)),
            )
        ),

        // Hilleri
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.POLECAT_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(1, 1), Date(12, 31)),
            )
        ),

        // Itämerennorppa
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.RINGED_SEAL_ID,
            startYear = 2021, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(4, 16), Date(12, 31)),
            )
        ),

        // Jouhisorsa
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.PINTAIL_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Punasotka + Tukkakoskelo
        // protected starting from 2020 -> no need to define season

        // Metsäkauris
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.ROE_DEER_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(9, 1), Date(2, 15)),
                DatePeriod(Date(5, 16), Date(6, 15)),
            )
        ),

        // Lapasorsa
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.SHOVELER_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Tukkasotka
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.TUFTED_DUCK_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Haapana
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.WIGEON_ID,
            startYear = 2020, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(8, 20), Date(12, 31)),
            )
        ),

        // Villisika
        HardcodedHarvestSeason(
            speciesCode = SpeciesCodes.WILD_BOAR_ID,
            startYear = 2017, endYear = null,
            yearlySeasonPeriods = listOf(
                DatePeriod(Date(1, 1), Date(12, 31)),
            )
        ),
    ).groupBy { it.speciesCode }

    override fun hasHarvestSeasons(huntingYear: HuntingYear): Boolean {
        return hardcodedSeasons.values.contains { hardcodedHarvestSeasons ->
            hardcodedHarvestSeasons.contains { season ->
                season.hasSeasonPeriodsForHuntingYear(huntingYear)
            }
        }
    }

    override fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason>? {
        return hardcodedSeasons[speciesCode]
            ?.mapNotNull { season ->
                season.toHarvestSeason(huntingYear)
            }
    }
}
