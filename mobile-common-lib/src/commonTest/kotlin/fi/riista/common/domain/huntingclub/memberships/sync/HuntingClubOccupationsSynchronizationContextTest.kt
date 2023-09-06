package fi.riista.common.domain.huntingclub.memberships.sync

import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.dto.OccupationDTO
import fi.riista.common.domain.dto.toOccupation
import fi.riista.common.domain.huntingclub.MockHuntingClubData
import fi.riista.common.domain.huntingclub.memberships.storage.MockHuntingClubOccupationsMemoryStorage
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.plus
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import fi.riista.common.util.deserializeFromJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HuntingClubOccupationsSynchronizationContextTest {

    private val usernamePentti = MockUserInfo.PenttiUsername

    @Test
    fun `occupations cannot be re-synchronized within 12 hours`() = runBlockingTest {
        val localDateTimeProvider = MockDateTimeProvider()
        val occupationsStorage = MockHuntingClubOccupationsMemoryStorage()
        val backendApi = BackendAPIMock()

        val synchronizationContext = HuntingClubOccupationsSynchronizationContext(
            usernameProvider = object : UsernameProvider {
                override val username = usernamePentti
            },
            occupationsFetcher = HuntingClubOccupationsBackendFetcher(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI = backendApi
                }
            ),
            occupationsStorage = occupationsStorage,
            preferences = MockPreferences(),
            localDateTimeProvider = localDateTimeProvider,
        )

        assertEquals(0, backendApi.callCount(BackendAPI::fetchHuntingClubMemberships.name), "call count 0")
        assertFalse(occupationsStorage.hasOccupations(username = usernamePentti))

        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)

        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingClubMemberships.name), "call count 1-1")
        assertTrue(occupationsStorage.hasOccupations(username = usernamePentti))
        assertNotNull(occupationsStorage.getOccupation(usernamePentti, id = 46))

        localDateTimeProvider.now = localDateTimeProvider.now.plus(minutes = 12 * 60)
        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, backendApi.callCount(BackendAPI::fetchHuntingClubMemberships.name), "call count 1-2")
        assertNotNull(occupationsStorage.getOccupation(usernamePentti, id = 46))

        localDateTimeProvider.now = localDateTimeProvider.now.plus(minutes = 1)
        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(2, backendApi.callCount(BackendAPI::fetchHuntingClubMemberships.name), "call count 2")
        assertNotNull(occupationsStorage.getOccupation(usernamePentti, id = 46))
    }

    @Test
    fun `synchronization replaces occupations`() = runBlockingTest {
        val localDateTimeProvider = MockDateTimeProvider()
        val occupationsStorage = MockHuntingClubOccupationsMemoryStorage()
        val backendApi = BackendAPIMock()

        val synchronizationContext = HuntingClubOccupationsSynchronizationContext(
            usernameProvider = object : UsernameProvider {
                override val username = MockUserInfo.PenttiUsername
            },
            occupationsFetcher = HuntingClubOccupationsBackendFetcher(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI = backendApi
                }
            ),
            occupationsStorage = occupationsStorage,
            preferences = MockPreferences(),
            localDateTimeProvider = localDateTimeProvider,
        )

        occupationsStorage.replaceOccupations(
            username = usernamePentti,
            occupations = MockHuntingClubData.HuntingClubMemberships
                .deserializeFromJson<List<OccupationDTO>>()
                ?.map { dto ->
                    dto.copy(id = 99).toOccupation()
                }
                ?: listOf()
        )

        assertEquals(1, occupationsStorage.getOccupations(usernamePentti).size)
        assertNotNull(occupationsStorage.getOccupation(usernamePentti, id = 99 /* should exist */))

        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)

        assertEquals(1, occupationsStorage.getOccupations(usernamePentti).size)
        assertNull(occupationsStorage.getOccupation(usernamePentti, id = 99 /* should not exist */))
        assertNotNull(occupationsStorage.getOccupation(usernamePentti, id = 46))
    }
}
