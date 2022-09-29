package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingControlSynchronizationContextTest {

    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testSync() = runBlockingTest {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val databaseDriver = dbDriverFactory.createDriver()
        val database = RiistaDatabase(driver = databaseDriver)
        val backendApi = BackendAPIMock()
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))

        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = dbDriverFactory,
            mockBackendAPI = backendApi,
            mockCurrentUserContextProvider = userContextProvider,
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )

        val context = HuntingControlSynchronizationContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider(),
            commonFileProvider = CommonFileProviderMock(),
            username = username,
        )

        context.synchronize()

        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingControlRhys.name))
        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingControlAttachmentThumbnail.name))
        assertEquals(
            MockHuntingControlData.FirstAttachmentId,
            backendApi.callParameter(BackendAPI::fetchHuntingControlAttachmentThumbnail.name)
        )
    }
}
