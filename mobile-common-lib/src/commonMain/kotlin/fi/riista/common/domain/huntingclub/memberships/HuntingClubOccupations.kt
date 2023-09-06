package fi.riista.common.domain.huntingclub.memberships

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubRepository
import fi.riista.common.domain.huntingclub.memberships.storage.HuntingClubOccupationsRepository
import fi.riista.common.domain.huntingclub.memberships.storage.HuntingClubOccupationsStorage
import fi.riista.common.domain.huntingclub.memberships.sync.HuntingClubOccupationsBackendFetcher
import fi.riista.common.domain.huntingclub.memberships.sync.HuntingClubOccupationsFetcher
import fi.riista.common.domain.huntingclub.memberships.sync.HuntingClubOccupationsSynchronizationContext
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class HuntingClubOccupations internal constructor(
    private val currentUserContextProvider: CurrentUserContextProvider,
    occupationsFetcher: HuntingClubOccupationsFetcher,
    occupationsStorage: HuntingClubOccupationsStorage,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): HuntingClubOccupationsProvider by occupationsStorage {

    internal val synchronizationContext = HuntingClubOccupationsSynchronizationContext(
        usernameProvider = currentUserContextProvider,
        occupationsFetcher = occupationsFetcher,
        occupationsStorage = occupationsStorage,
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
        occupationsFetcher = HuntingClubOccupationsBackendFetcher(backendApiProvider = backendApiProvider),
        occupationsStorage = HuntingClubOccupationsRepository(
            database = database,
            clubStorage = HuntingClubRepository(database)
        ),
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )

    fun initialize() {
        RiistaSDK.registerSynchronizationContext(synchronizationContext)
    }
}

