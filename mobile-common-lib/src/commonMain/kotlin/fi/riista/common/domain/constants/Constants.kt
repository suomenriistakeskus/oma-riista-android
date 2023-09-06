package fi.riista.common.domain.constants

import fi.riista.common.model.Date

object Constants {
    const val HARVEST_SPEC_VERSION = 10
    const val OBSERVATION_SPEC_VERSION = 5
    const val HUNTING_CONTROL_EVENT_SPEC_VERSION = 1
    const val SRVA_SPEC_VERSION = 2

    // metadata can be updated every 12 hours
    const val METADATA_UPDATE_COOLDOWN_MINUTES = 60 * 12

    // harvest seasons can be updated:
    // - every 3 hours for the current hunting year,
    // - every 12 hours for the next hunting year
    const val HARVEST_SEASONS_CURRENT_YEAR_UPDATE_COOLDOWN_MINUTES = 60 * 3
    const val HARVEST_SEASONS_NEXT_YEAR_UPDATE_COOLDOWN_MINUTES = 60 * 12
    // how many days before next hunting year we can start fetching harvest seasons
    const val HARVEST_SEASONS_NEXT_YEAR_UPDATE_ALLOWED_BEFORE_DAYS = 31

    // Permit update times
    // - metsahallitus permits can be updated once every 3 hour
    const val METSAHALLITUS_PERMIT_UPDATE_COOLDOWN_MINUTES = 3*60

    // Club occupations
    // - these should be reloaded after login so next refreshes are not that important
    // -> 12 hours
    const val HUNTING_CLUB_OCCUPATIONS_UPDATE_COOLDOWN_MINUTES = 12*60

    // Hunting year begins in 1st of August
    val FIRST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 8, dayOfMonth = 1)
    val LAST_DATE_OF_HUNTING_YEAR = Date(monthNumber = 7, dayOfMonth = 31)

    const val HUNTING_CLUB_OFFICIAL_CODE_LENGTH = 7
}
