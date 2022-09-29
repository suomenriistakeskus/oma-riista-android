package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.domain.huntingControl.HuntingControlRhyFromDatabaseProvider
import fi.riista.common.domain.huntingControl.sync.model.LoadRhyHuntingControlEvents
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.model.LocalizedString
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HuntingControlRhyFromDatabaseProviderTest {
    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testRhysCanBeFetched() = runBlocking {
        initializeRiistaSDK()
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val rhyProvider = huntingControlRhyFromDatabaseProvider(database)
        assertTrue(rhyProvider.loadStatus.value.notLoaded)

        writeTestDataToDb(database)
        rhyProvider.fetch(refresh = false)

        assertTrue(rhyProvider.loadStatus.value.loaded)
        assertNotNull(rhyProvider.rhys)
        assertEquals(1, rhyProvider.rhys!!.size)
    }

    @Test
    fun testForceRefreshForcesRefresh() = runBlocking {
        initializeRiistaSDK()
        val databaseDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(databaseDriverFactory.createDriver())
        val rhyProvider = huntingControlRhyFromDatabaseProvider(database)
        assertTrue(rhyProvider.loadStatus.value.notLoaded)

        writeTestDataToDb(database)
        rhyProvider.fetch(refresh = false)

        assertEquals("Test RHY fi", rhyProvider.rhys?.get(0)?.name?.fi)

        val updatedRhyAndEvents = SyncTestData.getRhyEvents()
        writeTestDataToDb(
            database, updatedRhyAndEvents.copy(
                rhy = updatedRhyAndEvents.rhy.copy(
                    name = LocalizedString(fi = "Uusi nimi", sv = null, en = null)
                )
            )
        )

        rhyProvider.fetch(refresh = false)
        assertEquals("Test RHY fi", rhyProvider.rhys?.get(0)?.name?.fi)

        rhyProvider.forceRefreshOnNextFetch()
        rhyProvider.fetch(refresh = false)
        assertEquals("Uusi nimi", rhyProvider.rhys?.get(0)?.name?.fi)

        // Force refresh should affect only one fetch
        writeTestDataToDb(
            database = database,
            rhyEvents = updatedRhyAndEvents.copy(
                rhy = updatedRhyAndEvents.rhy.copy(
                    name = LocalizedString(fi = "Vielä uudempi nimi", sv = null, en = null)
                )
            )
        )
        rhyProvider.fetch(refresh = false)
        assertEquals("Uusi nimi", rhyProvider.rhys?.get(0)?.name?.fi)

    }

    private fun writeTestDataToDb(
        database: RiistaDatabase,
        rhyEvents: LoadRhyHuntingControlEvents = SyncTestData.getRhyEvents(),
    ) {
        val updater = huntingControlRhyToDatabaseUpdater(database, MockUserInfo.PenttiUsername)
        runBlocking {
            updater.update(listOf(rhyEvents))
        }
    }

    private fun huntingControlRhyFromDatabaseProvider(
        database: RiistaDatabase,
    ): HuntingControlRhyFromDatabaseProvider {
        return HuntingControlRhyFromDatabaseProvider(
            database = database,
        )
    }

    private fun huntingControlRhyToDatabaseUpdater(
        database: RiistaDatabase,
        username: String = "user",
    ): HuntingControlRhyToDatabaseUpdater {
        return HuntingControlRhyToDatabaseUpdater(
            database = database,
            username = username,
        )
    }

    private fun initializeRiistaSDK() {
        val ucProvider = CurrentUserContextProviderFactory.createMocked()
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = createDatabaseDriverFactory(),
            mockBackendAPI = BackendAPIMock(),
            mockCurrentUserContextProvider = ucProvider,
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        ucProvider.userLoggedIn(userInfoDTO)
    }
}
