package fi.riista.common.domain.permit.metsahallitusPermit.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.permit.metsahallitusPermit.storage.MetsahallitusPermitStorage
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal class MetsahallitusPermitSynchronizationContext(
    private val usernameProvider: UsernameProvider,
    private val permitFetcher: MetsahallitusPermitFetcher,
    val permitStorage: MetsahallitusPermitStorage,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): AbstractSynchronizationContext(preferences, localDateTimeProvider, SyncDataPiece.METSAHALLITUS_PERMITS) {

    private suspend fun updatePermits() {
        val username = usernameProvider.username ?: kotlin.run {
            logger.v { "Cannot synchronize Metsahallitus permits, no username" }
            return
        }

        when (val result = permitFetcher.fetchPermits()) {
            is OperationResultWithData.Failure -> {
                logger.v { "Failed to fetch Metsahallitus permits (status = ${result.statusCode})" }
            }
            is OperationResultWithData.Success -> {
                logger.v { "Fetched ${result.data.size} Metsahallitus permits." }
                permitStorage.replacePermits(username = username, permits = result.data)
                saveLastSynchronizationTimeStamp(timestamp = localDateTimeProvider.now())
            }
        }
    }

    override suspend fun synchronize(config: SynchronizationConfig) {
        val lastSynchronizationTime = getLastSynchronizationTimeStamp().takeIf { config.forceContentReload.not() }
        val now = localDateTimeProvider.now()
        if (lastSynchronizationTime == null ||
            lastSynchronizationTime.minutesUntil(now) > Constants.METSAHALLITUS_PERMIT_UPDATE_COOLDOWN_MINUTES) {
            updatePermits()
        } else {
            logger.v { "Not synchronizing Metsahallitus permits (last synced $lastSynchronizationTime)" }
        }
    }
}
