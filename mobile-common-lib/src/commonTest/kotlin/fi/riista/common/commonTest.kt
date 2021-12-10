package fi.riista.common

import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.NetworkClient
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
