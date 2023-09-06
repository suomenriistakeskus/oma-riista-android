package fi.riista.common.domain.huntingclub.memberships.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingclub.memberships.storage.HuntingClubOccupationsStorage
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal class HuntingClubOccupationsSynchronizationContext(
    private val usernameProvider: UsernameProvider,
    private val occupationsFetcher: HuntingClubOccupationsFetcher,
    val occupationsStorage: HuntingClubOccupationsStorage,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): AbstractSynchronizationContext(preferences, localDateTimeProvider, SyncDataPiece.HUNTING_CLUB_OCCUPATIONS) {

    private suspend fun updateOccupations() {
        val username = usernameProvider.username ?: kotlin.run {
            logger.v { "Cannot synchronize hunting club occupations, no username" }
            return
        }

        when (val result = occupationsFetcher.fetchOccupations()) {
            is OperationResultWithData.Failure -> {
                logger.v { "Failed to fetch club occupations (status = ${result.statusCode})" }
            }
            is OperationResultWithData.Success -> {
                logger.v { "Fetched ${result.data.size} occupations." }
                occupationsStorage.replaceOccupations(username = username, occupations = result.data)
                saveLastSynchronizationTimeStamp(timestamp = localDateTimeProvider.now())
            }
        }
    }

    override suspend fun synchronize(config: SynchronizationConfig) {
        val lastSynchronizationTime = getLastSynchronizationTimeStamp().takeIf { config.forceContentReload.not() }
        val now = localDateTimeProvider.now()
        if (lastSynchronizationTime == null ||
            lastSynchronizationTime.minutesUntil(now) > Constants.HUNTING_CLUB_OCCUPATIONS_UPDATE_COOLDOWN_MINUTES) {
            updateOccupations()
        } else {
            logger.v { "Not synchronizing Metsahallitus permits (last synced $lastSynchronizationTime)" }
        }
    }
}
