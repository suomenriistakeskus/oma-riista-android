package fi.riista.common

import fi.riista.common.helpers.initializeMocked
import org.junit.Test
import kotlin.test.assertEquals

class RiistaCommonAndroidTest {

    @Test
    fun testPlatformIsAndroid() {
        RiistaSDK.initializeMocked()
        val sdkConfiguration = RiistaSDK.INSTANCE.sdkConfiguration
        assertEquals(PlatformName.ANDROID, sdkConfiguration.platform.name)
    }
}
