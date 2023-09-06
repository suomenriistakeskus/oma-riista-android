package fi.riista.common.authentication

import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountServiceTest {

    @Test
    fun `unregister persists timestamp to preferences`() = runBlockingTest {
        val preferences = MockPreferences()
        val accountService = accountService(preferences)

        assertNull(accountService.userAccountUnregistrationRequestDatetime)
        assertFalse(preferences.contains(AccountService.USER_UNREGISTRATION_REQUEST_TIME))

        val unregisterTimestamp = accountService.unregisterAccount()
        assertNotNull(unregisterTimestamp)

        assertEquals(
            LocalDateTime(2023, 3, 21, 15, 13, 55),
            unregisterTimestamp
        )
        assertEquals(unregisterTimestamp, accountService.userAccountUnregistrationRequestDatetime)
        assertTrue(preferences.contains(AccountService.USER_UNREGISTRATION_REQUEST_TIME))
        assertEquals(
            unregisterTimestamp,
            preferences.getString(AccountService.USER_UNREGISTRATION_REQUEST_TIME)?.toLocalDateTime()
        )
    }

    @Test
    fun `cancel-unregister clears persisted timestamp from preferences`() = runBlockingTest {
        val preferences = MockPreferences()
        val unregisterRequestDatetime = LocalDateTime(2023, 3, 23, 14, 56, 0)
        preferences.putString(AccountService.USER_UNREGISTRATION_REQUEST_TIME, unregisterRequestDatetime.toStringISO8601())

        val accountService = accountService(preferences)

        assertEquals(unregisterRequestDatetime, accountService.userAccountUnregistrationRequestDatetime)

        assertTrue(accountService.cancelUnregisterAccount())

        assertNull(accountService.userAccountUnregistrationRequestDatetime)
        assertFalse(preferences.contains(AccountService.USER_UNREGISTRATION_REQUEST_TIME))
    }

    private fun accountService(
        preferences: Preferences,
        backendAPI: BackendAPI = backendAPIMock,
    ): AccountService {
        return AccountService(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            currentUserContextProvider = currentUserContextProvider,
            preferences = preferences,
        )
    }

    private val backendAPIMock: BackendAPIMock = BackendAPIMock()
    private val currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked()
}
