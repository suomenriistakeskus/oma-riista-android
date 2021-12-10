package fi.riista.common

import fi.riista.common.network.BackendAPIMock
import kotlin.test.Test
import kotlin.test.assertEquals

class RiistaCommonIosTest {

    @Test
    fun testPlatformIsIOS() {
        val configuration = RiistaSdkConfiguration("1", "2", "https://oma.riista.fi")
        RiistaSDK.initializeMocked(configuration, BackendAPIMock())
        val sdkConfiguration = RiistaSDK.INSTANCE.sdkConfiguration
        assertEquals(PlatformName.IOS, sdkConfiguration.platform.name)
    }
}
