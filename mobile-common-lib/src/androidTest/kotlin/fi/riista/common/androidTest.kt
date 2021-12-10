package fi.riista.common

import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.network.BackendAPIMock
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
        )
        val sdkConfiguration = RiistaSDK.INSTANCE.sdkConfiguration
        assertEquals(PlatformName.ANDROID, sdkConfiguration.platform.name)
    }
}
