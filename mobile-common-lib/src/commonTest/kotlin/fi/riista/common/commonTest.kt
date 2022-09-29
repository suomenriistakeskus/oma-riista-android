package fi.riista.common

import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.NetworkClient
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RiistaCommonTest {
    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testVersionInfo() {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = createDatabaseDriverFactory(),
            mockBackendAPI = BackendAPIMock(),
            mockCurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(),
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )
        val versionInfo = RiistaSDK.versionInfo
        assertEquals(versionInfo.appVersion, "1")
        assertEquals(versionInfo.appBuild, "2")
        assertEquals(versionInfo.sdkVersion, RiistaSDK.SDK_VERSION)
    }

    @Test
    fun testInvalidServerAddress() {
        assertFailsWith<AssertionError> {
            // address must not contain trailing slash
            val configuration = RiistaSdkConfiguration("1", "2", "$serverAddress/")
            RiistaSDK.initializeMocked(
                sdkConfiguration = configuration,
                databaseDriverFactory = createDatabaseDriverFactory(),
                mockBackendAPI = BackendAPIMock(),
                mockCurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(),
                mockLocalDateTimeProvider = MockDateTimeProvider(),
                mockMainScopeProvider = MockMainScopeProvider(),
                mockFileProvider = CommonFileProviderMock(),
            )
        }
    }

    @Test
    fun testValidServerAddress() {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        val networkClient = NetworkClient(configuration)
        assertEquals(networkClient.serverBaseAddress, serverAddress)
    }
}
