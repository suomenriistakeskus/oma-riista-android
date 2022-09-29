package fi.riista.common.domain.userInfo

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class CurrentUserContextProviderTest {
    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun loginStatusIsInitiallyNotLoggedIn() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)
    }

    @Test
    fun loginStatusUpdatedWhenLoggingIn() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        provider.userLoggedIn(userInfoDTO)

        assertTrue(provider.userContext.loginStatus.value is LoginStatus.LoggedIn)
    }

    @Test
    fun loginStatusUpdatedWhenLoggingOut() {
        initializeRiistaSDK()
        val provider = CurrentUserContextProviderFactory.createMocked()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        provider.userLoggedIn(userInfoDTO)
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.LoggedIn)

        provider.userLoggedOut()
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)
    }

    @Test
    fun userInfoExistsAfterLoggingIn() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertNull(provider.userContext.userInfo)

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        provider.userLoggedIn(userInfoDTO)
        assertNotNull(provider.userContext.userInfo)
    }

    @Test
    fun userInfoClearedWhenLoggedOut() {
        initializeRiistaSDK()
        val provider = CurrentUserContextProviderFactory.createMocked()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        provider.userLoggedIn(userInfoDTO)
        assertNotNull(provider.userContext.userInfo)

        provider.userLoggedOut()
        assertNull(provider.userContext.userInfo)
        assertNull(provider.userContext.username)
    }

    @Test
    fun fallbackUsernameGivenIfProvidedWhenNotLoggedIn() {
        val username = "Seppo"
        initializeRiistaSDK()
        val preferences = MockPreferences()
        val key = "uc_fallback_username" // Copied from UserContext
        preferences.putString(key, username)
        val provider = CurrentUserContextProviderFactory.createMocked(preferences = preferences)
        assertEquals(username, provider.userContext.username)
    }

    private fun initializeRiistaSDK() {
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
    }
}
