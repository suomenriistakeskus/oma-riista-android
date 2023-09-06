package fi.riista.common.domain.season.sync

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.daysUntil
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences

internal class HarvestSeasonsFetcher(
    private val harvestSeason: Season,
    private val backendApiProvider: BackendApiProvider,
    private val preferences: Preferences,
) {
    enum class Season {
        PREVIOUS, // previous hunting year
        CURRENT, // the current hunting year
        NEXT, // the upcoming hunting year
        ;

        fun huntingYear(now: LocalDateTime): HuntingYear {
            val currentHuntingYear = now.date.getHuntingYear()
            return when (this) {
                PREVIOUS -> currentHuntingYear - 1
                CURRENT -> currentHuntingYear
                NEXT -> currentHuntingYear + 1
            }
        }
    }

    fun getHuntingYearToFetchFor(now: LocalDateTime): HuntingYear = harvestSeason.huntingYear(now)

    /**
     * Determines whether harvest seasons should be fetched.
     * @param hasSeasonsForHuntingYear   True if there are already some seasons for the desired hunting year.
     */
    fun shouldFetchHarvestSeasons(
        now: LocalDateTime,
        hasSeasonsForHuntingYear: Boolean,
        ignoreFetchCooldown: Boolean,
    ): Boolean {
        if (ignoreFetchCooldown) {
            logger.v { "Ignoring fetch cooldown for ${harvestSeason}_SEASON." }
        }

        return when (harvestSeason) {
            Season.PREVIOUS -> {
                // no need to fetch for previous season if harvest seasons have already been fetched for
                // that year after the year has ended.
                // - it is assumed that seasons won't be changed afterwards
                val lastFetchTime = getHarvestSeasonsFetchTime(huntingYear = getHuntingYearToFetchFor(now))
                val currentHuntingYear = now.date.getHuntingYear()

                lastFetchTime == null || ignoreFetchCooldown || lastFetchTime.date.getHuntingYear() < currentHuntingYear
            }
            Season.CURRENT -> !hasSeasonsForHuntingYear || ignoreFetchCooldown || isFetchCooldownCompleted(
                now, cooldownMinutes = Constants.HARVEST_SEASONS_CURRENT_YEAR_UPDATE_COOLDOWN_MINUTES
            )
            Season.NEXT -> {
                val firstDayOfUpcomingHuntingYear = Constants.FIRST_DATE_OF_HUNTING_YEAR.toLocalDate(year = getHuntingYearToFetchFor(now))
                val daysUntilNextHuntingYearStart = now.date.daysUntil(firstDayOfUpcomingHuntingYear)

                if (daysUntilNextHuntingYearStart > Constants.HARVEST_SEASONS_NEXT_YEAR_UPDATE_ALLOWED_BEFORE_DAYS) {
                    logger.v {
                        "Not fetching seasons for ${harvestSeason}_SEASON. Next hunting year " +
                                "$daysUntilNextHuntingYearStart days away."
                    }
                    false
                } else {
                    !hasSeasonsForHuntingYear || ignoreFetchCooldown || isFetchCooldownCompleted(
                        now, cooldownMinutes = Constants.HARVEST_SEASONS_NEXT_YEAR_UPDATE_COOLDOWN_MINUTES
                    )
                }
            }
        }
    }

    suspend fun fetchHarvestSeasons(now: LocalDateTime): List<HarvestSeasonDTO>? {
        val huntingYear = getHuntingYearToFetchFor(now)
        val response = backendApiProvider.backendAPI.fetchHarvestSeasons(huntingYear = huntingYear)

        response.onError { statusCode, exception ->
            logger.w { "Failed to fetch harvest seasons ($statusCode). ${exception?.cause} - ${exception?.message}" }
        }
        return response.transformSuccessData { _, data ->
            onSeasonsFetched(now)

            data.typed
        }
    }

    private fun onSeasonsFetched(now: LocalDateTime) {
        val huntingYear = getHuntingYearToFetchFor(now)
        saveHarvestSeasonsFetchTime(huntingYear, now)
    }

    private fun isFetchCooldownCompleted(now: LocalDateTime, cooldownMinutes: Int): Boolean {
        val huntingYear = getHuntingYearToFetchFor(now)

        val fetchTime = getHarvestSeasonsFetchTime(huntingYear)
            ?: kotlin.run {
                logger.v { "Not cooling down. No previous fetch time for $harvestSeason" }
                return true
            }

        return if (fetchTime.minutesUntil(now) > cooldownMinutes) {
            logger.v { "Cooldown completed. Fetching seasons for ${harvestSeason}_SEASON allowed" }
            true
        } else {
            logger.v { "Cooling down from previous ${harvestSeason}_SEASON fetch at $fetchTime" }
            false
        }
    }

    internal fun saveHarvestSeasonsFetchTime(huntingYear: HuntingYear, now: LocalDateTime) {
        logger.v { "Saving harvest seasons fetch time $now for ${harvestSeason}_SEASON." }
        preferences.putString(preferencesKeyForHuntingYearFetchTime(huntingYear), now.toStringISO8601())
    }

    private fun getHarvestSeasonsFetchTime(huntingYear: HuntingYear): LocalDateTime? {
        return preferences.getString(preferencesKeyForHuntingYearFetchTime(huntingYear))?.let { fetchTime ->
            LocalDateTime.parseLocalDateTime(fetchTime)
        }
    }

    companion object {
        private const val KEY_PREFIX = "HarvestSeasonsFetcher"

        private val logger by getLogger(HarvestSeasonsFetcher::class)

        private fun preferencesKeyForHuntingYearFetchTime(huntingYear: HuntingYear) =
            "${KEY_PREFIX}_FETCHED_FOR_$huntingYear"
    }
}