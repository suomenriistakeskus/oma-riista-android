package fi.riista.common.model

object Constants {
    const val HARVEST_SPEC_VERSION = 8
    const val OBSERVATION_SPEC_VERSION = 4

    // Hunting year begins in 1st of August
    val FIRST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 8, dayOfMonth = 1)
    val LAST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 7, dayOfMonth = 31)
}