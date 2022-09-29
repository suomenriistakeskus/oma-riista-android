package fi.riista.common

import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.util.MockDateTimeProvider
import org.junit.Test
import kotlin.test.assertEquals

class RiistaCommonAndroidTest {

    @Test
    fun testPlatformIsAndroid() {
        val configuration = RiistaSdkConfiguration("1", "2", "https://oma.riista.fi")
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = createDatabaseDriverFactory(),
            mockBackendAPI = BackendAPIMock(),
            mockCurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(),
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )
        val sdkConfiguration = RiistaSDK.INSTANCE.sdkConfiguration
        assertEquals(PlatformName.ANDROID, sdkConfiguration.platform.name)
    }
}
