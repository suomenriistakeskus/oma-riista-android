package fi.riista.common.domain.season

import fi.riista.common.RiistaSDK
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.domain.season.provider.BackendHarvestSeasonsProvider
import fi.riista.common.domain.season.provider.BackendHarvestSeasonsProviderImpl
import fi.riista.common.domain.season.provider.HardCodedHarvestSeasonProvider
import fi.riista.common.domain.season.provider.HarvestSeasonProvider
import fi.riista.common.domain.season.storage.HarvestSeasonsDatabaseRepository
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.isWithinPeriods
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class HarvestSeasons internal constructor(
    internal val backendSeasonsProvider: BackendHarvestSeasonsProvider,
) {
    internal constructor(
        databaseDriverFactory: DatabaseDriverFactory,
        backendApiProvider: BackendApiProvider,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
    ): this(
        backendSeasonsProvider = BackendHarvestSeasonsProviderImpl(
            harvestSeasonsRepository = HarvestSeasonsDatabaseRepository(databaseDriverFactory),
            backendApiProvider = backendApiProvider,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
        )
    )

    private val fallbackProvider: HarvestSeasonProvider = HardCodedHarvestSeasonProvider()


    fun isDuringHarvestSeason(speciesCode: SpeciesCode, date: LocalDate): Boolean {
        val huntingYear = date.getHuntingYear()

        val seasonPeriods: List<LocalDatePeriod> = getHarvestSeasons(speciesCode, huntingYear)
            .flatMap { season ->
                season.seasonPeriods
            }

        return date.isWithinPeriods(seasonPeriods)
    }

    private fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason> {
        val harvestSeasons = if (backendSeasonsProvider.hasHarvestSeasons(huntingYear)) {
            backendSeasonsProvider.getHarvestSeasons(speciesCode, huntingYear)
        } else {
            fallbackProvider.getHarvestSeasons(speciesCode, huntingYear)
        }

        return harvestSeasons ?: listOf()
    }

    internal fun initialize() {
        RiistaSDK.registerSynchronizationContext(backendSeasonsProvider.synchronizationContext)
    }
}
