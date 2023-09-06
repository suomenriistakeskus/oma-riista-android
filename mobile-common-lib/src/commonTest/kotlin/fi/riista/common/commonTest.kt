package fi.riista.common

import fi.riista.common.helpers.TestCrashlyticsLogger
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.network.NetworkClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RiistaCommonTest {
    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testVersionInfo() {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress, TestCrashlyticsLogger)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
        )
        val versionInfo = RiistaSDK.versionInfo
        assertEquals(versionInfo.appVersion, "1")
        assertEquals(versionInfo.appBuild, "2")
    }

    @Test
    fun testInvalidServerAddress() {
        assertFailsWith<AssertionError> {
            // address must not contain trailing slash
            val configuration = RiistaSdkConfiguration("1", "2", "$serverAddress/", TestCrashlyticsLogger)
            RiistaSDK.initializeMocked(
                sdkConfiguration = configuration,
            )
        }
    }

    @Test
    fun testValidServerAddress() {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress, TestCrashlyticsLogger)
        val networkClient = NetworkClient(configuration)
        assertEquals(networkClient.serverBaseAddress, serverAddress)
    }
}
