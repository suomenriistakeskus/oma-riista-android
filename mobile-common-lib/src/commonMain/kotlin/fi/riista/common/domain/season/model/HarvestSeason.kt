package fi.riista.common.domain.season.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.model.DatePeriod
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalizedString


data class HarvestSeason(
    /**
     * The species code for which this season is applicable.
     */
    val speciesCode: SpeciesCode,

    /**
     * The hunting year when season periods are valid.
     */
    val huntingYear: HuntingYear,


    /**
     * The date periods for seasons. Supports multiple seasons for each hunting year
     * e.g. 20.8 - 27.8 and 1.10 - 30.11 for Bean Goose.
     */
    val seasonPeriods: List<LocalDatePeriod>,

    /**
     * The name of the hunting season.
     */
    val name: LocalizedString?,
)



