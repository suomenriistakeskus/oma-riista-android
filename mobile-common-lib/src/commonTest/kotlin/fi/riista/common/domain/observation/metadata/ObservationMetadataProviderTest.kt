package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.dto.toObservationMetadata
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.metadata.network.ObservationMetadataFetcher
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.metadata.MetadataSpecification
import fi.riista.common.metadata.MockMetadataRepository
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.changeTime
import fi.riista.common.network.MockDataFetcher
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.MockDateTimeProvider
import fi.riista.common.util.deserializeFromJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservationMetadataProviderTest {

    @Test
    fun testFallbackMetadata() {
        assertEquals(HardcodedObservationMetadataProvider.metadata, getProvider().metadata)
    }

    @Test
    fun testMetadataCanBeFetchedFromCache() {
        val repository = MockMetadataRepository()
        repository.savedMetadatas[MetadataSpecification(
            metadataType = MetadataRepository.MetadataType.OBSERVATION,
            metadataSpecVersion = Constants.OBSERVATION_SPEC_VERSION.toLong(),
            metadataJsonFormatVersion = Constants.OBSERVATION_SPEC_VERSION.toLong(),
        )] =
            """
            {
              "lastModified" : "2020-03-18T11:11:47.703+0200",
              "speciesList" : [],
              "observationSpecVersion": 4
            }
            """.trimIndent()

        val provider = getProvider(metadataRepository = repository)

        val metadata = provider.metadata
        assertEquals("2020-03-18T11:11:47.703+0200", metadata.lastModified)
        assertEquals(4, metadata.observationSpecVersion)
        assertTrue(metadata.speciesMetadata.isEmpty())
    }

    @Test
    fun testMetadataCanBeFetched() = runBlockingTest {
        val provider = getProvider()
        provider.updateMetadata()

        assertEquals(mockMetadata, provider.metadata)
    }

    @Test
    fun testFetchedMetadataIsStoredToRepository() = runBlockingTest {
        val repository = MockMetadataRepository()
        val provider = getProvider(metadataRepository = repository)

        assertEquals(0, repository.savedMetadatas.count())
        provider.updateMetadata()
        assertEquals(1, repository.savedMetadatas.count())
    }

    @Test
    fun testFetchedMetadataIsStoredToRepositoryOnlyIfChanged() = runBlockingTest {
        val repository = MockMetadataRepository()
        val provider = getProvider(metadataRepository = repository)

        assertEquals(0, repository.savedMetadatas.count())
        provider.updateMetadata()
        assertEquals(1, repository.savedMetadatas.count())
        repository.savedMetadatas.clear()

        provider.updateMetadata()
        // should not be saved due to memory cache
        assertEquals(0, repository.savedMetadatas.count())
    }

    @Test
    fun testSynchronizationDoesNothingIfNotLoggedIn() = runBlockingTest {
        val fetcher = MockObservationMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            login = false,
        )

        assertEquals(0, fetcher.fetchCount)
        provider.synchronize()
        assertEquals(0, fetcher.fetchCount)
    }

    @Test
    fun testSynchronizationIsThrottled() = runBlockingTest {
        val fetcher = MockObservationMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val currentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
            localDateTimeProvider = dateTimeProvider,
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            currentUserContextProvider = currentUserContextProvider
        )

        assertEquals(0, fetcher.fetchCount)
        provider.synchronize()
        assertEquals(1, fetcher.fetchCount)

        provider.synchronize()
        assertEquals(1, fetcher.fetchCount, "second update")

        dateTimeProvider.now = dateTimeProvider.now.changeTime(hour = 20, minute = 0)
        provider.synchronize()
        assertEquals(1, fetcher.fetchCount, "update after time change 1")

        // throttling threshold is currently 12 hours
        dateTimeProvider.now = dateTimeProvider.now.changeTime(hour = 20, minute = 1)
        provider.synchronize()
        assertEquals(2, fetcher.fetchCount, "update after time change 2")
    }

    private val mockMetadata = MockObservationMetadata.METADATA_SPEC_VERSION_4
        .deserializeFromJson<ObservationMetadataDTO>()!!
        .toObservationMetadata()

    private fun getProvider(
        metadataRepository: MetadataRepository = MockMetadataRepository(),
        fetchedMetadata: ObservationMetadata = mockMetadata,
        metadataFetcher: ObservationMetadataFetcher = MockObservationMetadataFetcher(fetchedMetadata),
        preferences: Preferences = MockPreferences(),
        localDateTimeProvider: LocalDateTimeProvider = MockDateTimeProvider(),
        currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider
        ),
        login: Boolean = true
    ): ObservationMetadataProvider {
        if (login) {
            currentUserContextProvider.userLoggedIn(MockUserInfo.Pentti.deserializeFromJson()!!)
        }

        return ObservationMetadataProvider(
            metadataRepository = metadataRepository,
            metadataFetcher = metadataFetcher,
            currentUserContextProvider = currentUserContextProvider,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
        )
    }
}

private class MockObservationMetadataFetcher(
    mockData: ObservationMetadata
) : MockDataFetcher<ObservationMetadata>(mockData), ObservationMetadataFetcher {
    override val metadata: ObservationMetadata?
        get() = fetchedData
}

