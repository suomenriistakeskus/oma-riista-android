package fi.riista.common.domain.model

import fi.riista.common.domain.constants.Constants
import fi.riista.common.model.LocalDate
import fi.riista.common.model.getDateWithoutYear

typealias HuntingYear = Int

fun HuntingYear.toSeasonString(): String {
    val nextYearString = (this + 1).toString()
    return "$this - ${nextYearString.drop(2)}"
}

fun LocalDate.getHuntingYear(): HuntingYear {
    return if (getDateWithoutYear() >= Constants.FIRST_DATE_OF_HUNTING_YEAR) {
        year
    } else {
        year - 1
    }
}