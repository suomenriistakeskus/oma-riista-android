package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubRepository
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubStorage
import fi.riista.common.domain.huntingclub.memberships.HuntingClubOccupationsProvider
import fi.riista.common.domain.huntingclub.memberships.storage.HuntingClubOccupationsRepository
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.network.BackendApiProvider

internal class HuntingClubsSelectableForEntriesFactory(
    private val usernameProvider: UsernameProvider,
    private val clubStorage: HuntingClubStorage,
    private val clubOccupationsProvider: HuntingClubOccupationsProvider,
    private val selectedForEntriesStorage: UserHuntingClubsProvider,
    private val backendApiProvider: BackendApiProvider,
): HuntingClubsSelectableForEntries.Factory {
    private constructor(
        usernameProvider: UsernameProvider,
        database: RiistaDatabase,
        clubStorage: HuntingClubStorage,
        backendApiProvider: BackendApiProvider,
    ): this(
        usernameProvider = usernameProvider,
        clubStorage = clubStorage,
        clubOccupationsProvider = HuntingClubOccupationsRepository(database, clubStorage),
        selectedForEntriesStorage = HuntingClubsSelectedForEntriesRepository(database, clubStorage),
        backendApiProvider = backendApiProvider,
    )

    internal constructor(
        usernameProvider: UsernameProvider,
        database: RiistaDatabase,
        backendApiProvider: BackendApiProvider,
    ): this(
        usernameProvider = usernameProvider,
        database = database,
        clubStorage = HuntingClubRepository(database),
        backendApiProvider = backendApiProvider,
    )

    override fun create(): HuntingClubsSelectableForEntries =
        HuntingClubsSelectableForEntriesImpl(
            usernameProvider = usernameProvider,
            clubStorage = clubStorage,
            clubOccupationsProvider = clubOccupationsProvider,
            selectedClubsForEntriesStorage = selectedForEntriesStorage,
            backendApiProvider = backendApiProvider,
        )
}
