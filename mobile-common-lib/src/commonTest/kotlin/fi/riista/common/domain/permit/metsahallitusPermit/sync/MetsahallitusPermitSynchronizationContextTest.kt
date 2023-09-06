package fi.riista.common.domain.permit.metsahallitusPermit.sync

import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.permit.metsahallitusPermit.dto.CommonMetsahallitusPermitDTO
import fi.riista.common.domain.permit.metsahallitusPermit.dto.toCommonMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.storage.MetsahallitusPermitStorage
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

class MetsahallitusPermitSynchronizationContextTest {

    private val usernamePentti = MockUserInfo.PenttiUsername

    @Test
    fun `permits cannot be re-synchronized within three hours`() = runBlockingTest {
        val localDateTimeProvider = MockDateTimeProvider()
        val permitStorage = MockPermitStorage()
        val backendApi = BackendAPIMock()

        val synchronizationContext = MetsahallitusPermitSynchronizationContext(
            usernameProvider = object : UsernameProvider {
                override val username = usernamePentti
            },
            permitFetcher = MetsahallitusPermitBackendFetcher(
                backendApiProvider = object: BackendApiProvider {
                    override val backendAPI = backendApi
                }
            ),
            permitStorage = permitStorage,
            preferences = MockPreferences(),
            localDateTimeProvider = localDateTimeProvider,
        )

        assertEquals(0, backendApi.callCount(BackendAPI::fetchMetsahallitusPermits.name), "call count 0")
        assertFalse(permitStorage.hasPermits(username = usernamePentti))

        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)

        assertEquals(1, backendApi.callCount(BackendAPI::fetchMetsahallitusPermits.name), "call count 1-1")
        assertTrue(permitStorage.hasPermits(username = usernamePentti))
        assertNotNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "4949110101"))

        localDateTimeProvider.now = localDateTimeProvider.now.plus(minutes = 3*60)
        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, backendApi.callCount(BackendAPI::fetchMetsahallitusPermits.name), "call count 1-2")
        assertNotNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "4949110101"))

        localDateTimeProvider.now = localDateTimeProvider.now.plus(minutes = 1)
        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(2, backendApi.callCount(BackendAPI::fetchMetsahallitusPermits.name), "call count 2")
        assertNotNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "4949110101"))
    }

    @Test
    fun `synchronization replaces permits`() = runBlockingTest {
        val localDateTimeProvider = MockDateTimeProvider()
        val permitStorage = MockPermitStorage()
        val backendApi = BackendAPIMock()

        val synchronizationContext = MetsahallitusPermitSynchronizationContext(
            usernameProvider = object : UsernameProvider {
                override val username = MockUserInfo.PenttiUsername
            },
            permitFetcher = MetsahallitusPermitBackendFetcher(
                backendApiProvider = object: BackendApiProvider {
                    override val backendAPI = backendApi
                }
            ),
            permitStorage = permitStorage,
            preferences = MockPreferences(),
            localDateTimeProvider = localDateTimeProvider,
        )

        permitStorage.replacePermits(
            username = usernamePentti,
            permits = MockMetsahallitusPermitsData.permits
                .deserializeFromJson<List<CommonMetsahallitusPermitDTO>>()
                ?.map { dto ->
                    dto.copy(permitIdentifier = "foo_permit")
                        .toCommonMetsahallitusPermit()
                }
                ?: listOf()
        )

        assertEquals(1, permitStorage.getAllPermits(usernamePentti).size)
        assertNotNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "foo_permit"))

        synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)

        assertEquals(1, permitStorage.getAllPermits(usernamePentti).size)
        assertNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "foo_permit"))
        assertNotNull(permitStorage.getPermit(usernamePentti, permitIdentifier = "4949110101"))
    }
}

private class MockPermitStorage: MetsahallitusPermitStorage {
    val permitsByUser = mutableMapOf<String, List<CommonMetsahallitusPermit>>()

    override fun hasPermits(username: String): Boolean =
        getAllPermits(username).isNotEmpty()

    override fun getAllPermits(username: String): List<CommonMetsahallitusPermit> =
        permitsByUser[username] ?: listOf()

    override fun getPermit(username: String, permitIdentifier: String): CommonMetsahallitusPermit? {
        return permitsByUser[username]?.firstOrNull { it.permitIdentifier == permitIdentifier }
    }

    override suspend fun replacePermits(username: String, permits: List<CommonMetsahallitusPermit>) {
        permitsByUser[username] = permits
    }
}
