package fi.riista.common.domain.permit.metsahallitusPermit

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.permit.metsahallitusPermit.storage.MetsahallitusPermitRepository
import fi.riista.common.domain.permit.metsahallitusPermit.storage.MetsahallitusPermitStorage
import fi.riista.common.domain.permit.metsahallitusPermit.sync.MetsahallitusPermitBackendFetcher
import fi.riista.common.domain.permit.metsahallitusPermit.sync.MetsahallitusPermitFetcher
import fi.riista.common.domain.permit.metsahallitusPermit.sync.MetsahallitusPermitSynchronizationContext
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class MetsahallitusPermits internal constructor(
    private val currentUserContextProvider: CurrentUserContextProvider,
    permitFetcher: MetsahallitusPermitFetcher,
    permitStorage: MetsahallitusPermitStorage,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): MetsahallitusPermitProvider by permitStorage {

    internal val synchronizationContext = MetsahallitusPermitSynchronizationContext(
        usernameProvider = currentUserContextProvider,
        permitFetcher = permitFetcher,
        permitStorage = permitStorage,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )

    internal constructor(
        currentUserContextProvider: CurrentUserContextProvider,
        backendApiProvider: BackendApiProvider,
        database: RiistaDatabase,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
    ): this(
        currentUserContextProvider = currentUserContextProvider,
        permitFetcher = MetsahallitusPermitBackendFetcher(backendApiProvider = backendApiProvider),
        permitStorage = MetsahallitusPermitRepository(database),
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )

    fun initialize() {
        RiistaSDK.registerSynchronizationContext(synchronizationContext)
    }
}
