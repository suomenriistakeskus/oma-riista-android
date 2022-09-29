package fi.riista.common.domain.constants

import fi.riista.common.model.Date

object Constants {
    const val HARVEST_SPEC_VERSION = 9
    const val OBSERVATION_SPEC_VERSION = 4
    const val HUNTING_CONTROL_EVENT_SPEC_VERSION = 1
    const val SRVA_SPEC_VERSION = 2

    // metadata can be updated every 12 hours
    const val METADATA_UPDATE_COOLDOWN_MINUTES = 60 * 12

    // Hunting year begins in 1st of August
    val FIRST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 8, dayOfMonth = 1)
    val LAST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 7, dayOfMonth = 31)
}
