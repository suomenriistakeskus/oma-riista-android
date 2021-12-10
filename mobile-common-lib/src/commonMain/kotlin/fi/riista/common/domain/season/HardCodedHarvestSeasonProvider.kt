package fi.riista.common.domain.season

import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.model.Date
import fi.riista.common.model.DatePeriod
import fi.riista.common.model.SpeciesCode
import fi.riista.common.model.SpeciesCodes

class HardCodedHarvestSeasonProvider : HarvestSeasonProvider {
    private val hardcodedSeasons: Map<SpeciesCode, List<HuntingSeason>> = mapOf(
            // Metsähanhi
            SpeciesCodes.BEAN_GOOSE_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(8, 27)),
                            DatePeriod(Date(10, 1), Date(11, 30)),
                    )
            ),

            // Karhu
            SpeciesCodes.BEAR_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(10, 31)),
                    )
            ),

            // Haahka
            SpeciesCodes.COMMON_EIDER_ID to HuntingSeason(
                    startYear = 2019, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(6, 1), Date(6, 15)),
                    )
            ),

            // Nokikana
            SpeciesCodes.COOT_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Heinätavi
            SpeciesCodes.GARGANEY_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Isokoskelo
            SpeciesCodes.GOOSANDER_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(9, 1), Date(12, 31)),
                    )
            ),

            // Merihanhi
            SpeciesCodes.GREYLAG_GOOSE_ID to HuntingSeason(
                    startYear = 2021, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 10), Date(12, 31)),
                    )
            ),

            // Halli
            SpeciesCodes.GREY_SEAL_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(4, 16), Date(12, 31)),
                    )
            ),

            // Alli
            SpeciesCodes.LONG_TAILED_DUCK_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(9, 1), Date(12, 31)),
                    )
            ),

            // Hilleri
            SpeciesCodes.POLECAT_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(1, 1), Date(12, 31)),
                    )
            ),

            // Itämerennorppa
            SpeciesCodes.RINGED_SEAL_ID to HuntingSeason(
                    startYear = 2021, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(4, 16), Date(12, 31)),
                    )
            ),

            // Jouhisorsa
            SpeciesCodes.PINTAIL_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Punasotka + Tukkakoskelo
            // protected starting from 2020 -> no need to define season

            // Metsäkauris
            SpeciesCodes.ROE_DEER_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(9, 1), Date(2, 15)),
                            DatePeriod(Date(5, 16), Date(6, 15)),
                    )
            ),

            // Lapasorsa
            SpeciesCodes.SHOVELER_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Tukkasotka
            SpeciesCodes.TUFTED_DUCK_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Haapana
            SpeciesCodes.WIGEON_ID to HuntingSeason(
                    startYear = 2020, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(8, 20), Date(12, 31)),
                    )
            ),

            // Villisika
            SpeciesCodes.WILD_BOAR_ID to HuntingSeason(
                    startYear = 2017, endYear = null,
                    yearlySeasonPeriods = listOf(
                            DatePeriod(Date(1, 1), Date(12, 31)),
                    )
            ),
    ).mapValues { listOf(it.value) }

    override fun getSpeciesHavingSeasons(): List<SpeciesCode> {
       return hardcodedSeasons.keys.toList()
    }

    override fun getHuntingSeasons(speciesCode: SpeciesCode): List<HuntingSeason> {
        return hardcodedSeasons[speciesCode] ?: listOf()
    }
}
