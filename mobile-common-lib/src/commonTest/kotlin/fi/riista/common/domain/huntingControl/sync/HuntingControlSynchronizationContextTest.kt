package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingControlSynchronizationContextTest {

    @Test
    fun testSync() {
        val dbDriverFactory = createDatabaseDriverFactory()
        val databaseDriver = dbDriverFactory.createDriver()
        val database = RiistaDatabase(driver = databaseDriver)
        val backendApi = BackendAPIMock()
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        RiistaSDK.initializeMocked(
            databaseDriverFactory = dbDriverFactory,
            mockBackendAPI = backendApi,
            mockCurrentUserContextProvider = userContextProvider,
        )

        val context = HuntingControlSynchronizationContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider(),
            commonFileProvider = CommonFileProviderMock(),
            currentUserContextProvider = userContextProvider,
        )

        runBlocking {
            context.startSynchronization(config = SynchronizationConfig.DEFAULT)
        }

        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingControlRhys.name))
        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingControlAttachmentThumbnail.name))
        assertEquals(
            MockHuntingControlData.FirstAttachmentId,
            backendApi.callParameter(BackendAPI::fetchHuntingControlAttachmentThumbnail.name)
        )
    }
}
