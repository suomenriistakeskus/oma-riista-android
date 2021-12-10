package fi.riista.common.userInfo

import fi.riista.common.dto.MockUserInfo
import kotlin.test.*

class CurrentUserContextProviderTest {

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
        val provider = CurrentUserContextProviderFactory.createMocked()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        provider.userLoggedIn(userInfoDTO)
        assertNotNull(provider.userContext.userInfo)

        provider.userLoggedOut()
        assertNull(provider.userContext.userInfo)
    }
}