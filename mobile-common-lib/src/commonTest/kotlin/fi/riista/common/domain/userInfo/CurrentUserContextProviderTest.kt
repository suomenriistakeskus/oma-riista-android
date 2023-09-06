package fi.riista.common.domain.userInfo

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.dto.toUserInformation
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.preferences.MockPreferences
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrentUserContextProviderTest {

    @Test
    fun `login status is initially NotLoggedIn`() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)
    }

    @Test
    fun `login status is updated when logging in`() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        runBlocking {
            provider.userLoggedIn(userInfoDTO)
        }

        assertTrue(provider.userContext.loginStatus.value is LoginStatus.LoggedIn)
    }

    @Test
    fun `login status is updated when logging out`() {
        initializeRiistaSDK()
        val provider = CurrentUserContextProviderFactory.createMocked()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        runBlocking {
            provider.userLoggedIn(userInfoDTO)
            assertTrue(provider.userContext.loginStatus.value is LoginStatus.LoggedIn)

            provider.userLoggedOut()
            assertTrue(provider.userContext.loginStatus.value is LoginStatus.NotLoggedIn)
        }
    }

    @Test
    fun `user information exists after logging in`() {
        val provider = CurrentUserContextProviderFactory.createMocked()
        assertNull(provider.userContext.userInformation)

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        runBlocking {
            provider.userLoggedIn(userInfoDTO)
        }
        assertNotNull(provider.userContext.userInformation)
    }

    @Test
    fun `user information is stored to repository when logging in`() {
        val userInformationRepository = MockUserInformationRepository()
        val provider = CurrentUserContextProviderFactory.createMocked(
            userInformationRepository = userInformationRepository
        )
        assertNull(provider.userContext.userInformation)

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        assertNull(userInformationRepository.getUserInformation(username = userInfoDTO.username))
        runBlocking {
            provider.userLoggedIn(userInfoDTO)
        }
        assertNotNull(provider.userContext.userInformation)
        assertNotNull(userInformationRepository.getUserInformation(username = userInfoDTO.username))
    }

    @Test
    fun `user information can exist before logging in`() {
        val userInformationRepository = MockUserInformationRepository()
        val preferences = MockPreferences()

        val userInformation = MockUserInfo.parse(MockUserInfo.Pentti).toUserInformation()
        runBlocking {
            userInformationRepository.saveUserInformation(userInformation)
        }

        val provider = CurrentUserContextProviderFactory.createMocked(
            userInformationRepository = userInformationRepository,
            preferences = preferences
        )

        // user information should be null right now since UserContext does not know the username
        assertEquals(LoginStatus.NotLoggedIn, provider.userContext.loginStatus.value)
        assertNull(provider.userContext.username)
        assertNull(provider.userContext.userInformation)

        // set the username and check again
        preferences.putString(UserContext.FALLBACK_USERNAME_KEY, userInformation.username)

        assertEquals(LoginStatus.NotLoggedIn, provider.userContext.loginStatus.value)
        assertNotNull(provider.userContext.username)
        assertNotNull(provider.userContext.userInformation)
        assertEquals(userInformation.username, provider.userContext.userInformation?.username)
        assertEquals(userInformation.username, provider.userContext.username)
    }

    @Test
    fun `user information is cleared after logging out`() {
        initializeRiistaSDK()
        val provider = CurrentUserContextProviderFactory.createMocked()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        runBlocking {
            provider.userLoggedIn(userInfoDTO)
            assertNotNull(provider.userContext.userInformation)

            provider.userLoggedOut()
            assertNull(provider.userContext.userInformation)
            assertNull(provider.userContext.username)
        }
    }

    @Test
    fun `fallback username is provided when login status is NotLoggedIn`() {
        val username = "Seppo"
        initializeRiistaSDK()
        val preferences = MockPreferences()
        preferences.putString(UserContext.FALLBACK_USERNAME_KEY, username)
        val provider = CurrentUserContextProviderFactory.createMocked(preferences = preferences)
        assertEquals(username, provider.userContext.username)
    }

    @Test
    fun `migrating user information is not alloed if there already is stored user information`() {
        val userInformationRepository = MockUserInformationRepository()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        val userInformation = userInfoDTO.toUserInformation()
        runBlocking {
            userInformationRepository.saveUserInformation(userInformation)
        }

        assertEquals(
            expected = 1,
            actual = userInformationRepository.userInformations.size
        )

        val provider = CurrentUserContextProviderFactory.createMocked(
            userInformationRepository = userInformationRepository,
        )

        assertEquals(
            expected = UserInformationMigrationResult.ALREADY_MIGRATED,
            actual = runBlocking {
                provider.userContext.migrateUserInformationFromApplication(userInfoDTO)
            }
        )
        assertEquals(
            expected = 1,
            actual = userInformationRepository.userInformations.size
        )
    }

    @Test
    fun `migrating user information is refused if usernames do not match`() {
        val userInformationRepository = MockUserInformationRepository()
        val preferences = MockPreferences()
        preferences.putString(UserContext.FALLBACK_USERNAME_KEY, "ei ainakaan pentti")

        val provider = CurrentUserContextProviderFactory.createMocked(
            userInformationRepository = userInformationRepository,
            preferences = preferences
        )

        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        assertEquals(
            expected = UserInformationMigrationResult.REFUSED,
            actual = runBlocking {
                provider.userContext.migrateUserInformationFromApplication(userInfoDTO)
            }
        )
        assertEquals(
            expected = 0,
            actual = userInformationRepository.userInformations.size
        )
    }

    @Test
    fun `migrating user information stores user information to database`() {
        val userInformationRepository = MockUserInformationRepository()
        val preferences = MockPreferences()
        val userInfoDTO = MockUserInfo.parse(MockUserInfo.Pentti)
        preferences.putString(UserContext.FALLBACK_USERNAME_KEY, userInfoDTO.username)
        assertEquals(
            expected = 0,
            actual = userInformationRepository.userInformations.size
        )

        val provider = CurrentUserContextProviderFactory.createMocked(
            userInformationRepository = userInformationRepository,
            preferences = preferences
        )

        assertEquals(
            expected = UserInformationMigrationResult.MIGRATED,
            actual = runBlocking {
                provider.userContext.migrateUserInformationFromApplication(userInfoDTO)
            }
        )
        assertEquals(
            expected = 1,
            actual = userInformationRepository.userInformations.size
        )
    }

    private fun initializeRiistaSDK() {
        RiistaSDK.initializeMocked()
    }
}
