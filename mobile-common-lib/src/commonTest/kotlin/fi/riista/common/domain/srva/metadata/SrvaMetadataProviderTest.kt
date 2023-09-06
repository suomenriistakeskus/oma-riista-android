package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.metadata.dto.toSrvaMetadata
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.metadata.network.SrvaMetadataFetcher
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.metadata.MetadataSpecification
import fi.riista.common.metadata.MockMetadataRepository
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.changeTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.MockDataFetcher
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.MockDateTimeProvider
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SrvaMetadataProviderTest {

    @Test
    fun testFallbackMetadata() {
        assertEquals(HardcodedSrvaMetadataProvider.metadata, getProvider().metadata)
    }

    @Test
    fun testMetadataCanBeFetchedFromCache() {
        val repository = MockMetadataRepository()
        repository.savedMetadatas[MetadataSpecification(
            metadataType = MetadataRepository.MetadataType.SRVA,
            metadataSpecVersion = Constants.SRVA_SPEC_VERSION.toLong(),
            metadataJsonFormatVersion = 1L
        )] =
            """
            {
                "speciesCodes":[47503],
                "ages":["ADULT"],
                "genders":["FEMALE"],
                "eventCategories":[
                    {
                        "categoryType":"ACCIDENT",
                        "possibleEventTypes":["TRAFFIC_ACCIDENT"],
                        "possibleEventTypeDetails":{
                            "TRAFFIC_ACCIDENT": [
                                { "detailType":"CARED_HOUSE_AREA", "speciesCodes":[] },
                                { "detailType":"FARM_ANIMAL_BUILDING", "speciesCodes":[ 123 ] }
                            ]
                        },
                        "possibleEventResults":["ANIMAL_FOUND_DEAD"],
                        "possibleEventResultDetails":{
                            "ANIMAL_DEPORTED": [ "ANIMAL_CONTACTED" ]
                        },
                        "possibleMethods":["TRACED_WITH_DOG"]
                    }
                ]
            }
            """.trimIndent()

        val provider = getProvider(metadataRepository = repository)

        val metadata = provider.metadata
        assertEquals(1, metadata.species.count())
        assertEquals(47503, metadata.species[0].speciesCode)
        assertEquals(1, metadata.ages.count())
        assertEquals(GameAge.ADULT, metadata.ages[0].value)
        assertEquals(1, metadata.genders.count())
        assertEquals(Gender.FEMALE, metadata.genders[0].value)
        assertEquals(1, metadata.eventCategories.count())
        with (metadata.eventCategories[0]) {
            assertEquals(SrvaEventCategoryType.ACCIDENT, categoryType.value)
            assertEquals(1, possibleEventTypes.count())
            assertEquals(SrvaEventType.TRAFFIC_ACCIDENT, possibleEventTypes[0].value)
            assertEquals(1, possibleEventTypeDetails.size)
            with (possibleEventTypeDetails.entries.first()) {
                assertEquals(SrvaEventType.TRAFFIC_ACCIDENT.toBackendEnum(), key)
                assertEquals(2, value.size)
                with (value[0]) {
                    assertEquals(SrvaEventTypeDetail.CARED_HOUSE_AREA.toBackendEnum(), detailType)
                    assertTrue(speciesCodes.isEmpty())
                }
                with (value[1]) {
                    assertEquals(SrvaEventTypeDetail.FARM_ANIMAL_BUILDING.toBackendEnum(), detailType)
                    assertEquals(1, speciesCodes.size)
                    assertEquals(123, speciesCodes[0])
                }
            }
            assertEquals(1, possibleEventResults.count())
            assertEquals(SrvaEventResult.ANIMAL_FOUND_DEAD, possibleEventResults[0].value)
            assertEquals(1, possibleEventResultDetails.size)
            with (possibleEventResultDetails.entries.first()) {
                assertEquals(SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(), key)
                assertEquals(1, value.size)
                assertEquals(SrvaEventResultDetail.ANIMAL_CONTACTED.toBackendEnum(), value[0])
            }
            assertEquals(1, possibleMethods.count())
            assertEquals(SrvaMethodType.TRACED_WITH_DOG, possibleMethods[0].value)
        }
    }

    @Test
    fun testMetadataCanBeFetched() = runBlockingTest {
        val provider = getProvider()
        provider.startSynchronization(config = SynchronizationConfig(forceContentReload = true))

        assertEquals(mockMetadata, provider.metadata)
    }

    @Test
    fun testFetchedMetadataIsStoredToRepository() = runBlockingTest {
        val repository = MockMetadataRepository()
        val provider = getProvider(metadataRepository = repository)

        assertEquals(0, repository.savedMetadatas.count())
        provider.startSynchronization(config = SynchronizationConfig(forceContentReload = true))
        assertEquals(1, repository.savedMetadatas.count())
    }

    @Test
    fun testFetchedMetadataIsStoredToRepositoryOnlyIfChanged() = runBlockingTest {
        val repository = MockMetadataRepository()
        val provider = getProvider(metadataRepository = repository)

        assertEquals(0, repository.savedMetadatas.count())
        provider.startSynchronization(config = SynchronizationConfig(forceContentReload = true))
        assertEquals(1, repository.savedMetadatas.count())
        repository.savedMetadatas.clear()

        provider.startSynchronization(config = SynchronizationConfig(forceContentReload = true))
        // should not be saved due to memory cache
        assertEquals(0, repository.savedMetadatas.count())
    }

    @Test
    fun testSynchronizationDoesNothingIfNotLoggedIn() = runBlockingTest {
        val fetcher = MockSrvaMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            login = false,
        )

        assertEquals(0, fetcher.fetchCount)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(0, fetcher.fetchCount)
    }

    @Test
    fun testSynchronizationDoesNothingIfSrvaNotEnabled() = runBlockingTest {
        val fetcher = MockSrvaMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val currentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            currentUserContextProvider = currentUserContextProvider
        )

        // login with non-enabled srva
        val userInfo: UserInfoDTO = MockUserInfo.Pentti.deserializeFromJson()!!
        currentUserContextProvider.userLoggedIn(userInfo.copy(enableSrva = false))

        assertEquals(0, fetcher.fetchCount)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(0, fetcher.fetchCount)
    }

    @Test
    fun testSynchronizationIsThrottled() = runBlockingTest {
        val fetcher = MockSrvaMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val currentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            currentUserContextProvider = currentUserContextProvider
        )

        // enable srva for the user
        val userInfo: UserInfoDTO = MockUserInfo.Pentti.deserializeFromJson()!!
        currentUserContextProvider.userLoggedIn(userInfo.copy(enableSrva = true))

        assertEquals(0, fetcher.fetchCount)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, fetcher.fetchCount)

        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, fetcher.fetchCount, "second update")

        dateTimeProvider.now = dateTimeProvider.now.changeTime(hour = 20, minute = 0)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, fetcher.fetchCount, "update after time change 1")

        // throttling threshold is currently 12 hours
        dateTimeProvider.now = dateTimeProvider.now.changeTime(hour = 20, minute = 1)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(2, fetcher.fetchCount, "update after time change 2")
    }

    @Test
    fun `test synchronization can be forced`() = runBlockingTest {
        val fetcher = MockSrvaMetadataFetcher(mockMetadata)
        val dateTimeProvider = MockDateTimeProvider(
            now = LocalDateTime(2022, 1, 1, 8, 0, 0)
        )
        val currentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
        )
        val provider = getProvider(
            metadataFetcher = fetcher,
            localDateTimeProvider = dateTimeProvider,
            currentUserContextProvider = currentUserContextProvider
        )

        // enable srva for the user
        val userInfo: UserInfoDTO = MockUserInfo.Pentti.deserializeFromJson()!!
        currentUserContextProvider.userLoggedIn(userInfo.copy(enableSrva = true))

        assertEquals(0, fetcher.fetchCount)
        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, fetcher.fetchCount)

        provider.startSynchronization(config = SynchronizationConfig.DEFAULT)
        assertEquals(1, fetcher.fetchCount, "second update")

        provider.startSynchronization(config = SynchronizationConfig(forceContentReload = true))
        assertEquals(2, fetcher.fetchCount, "second update, 2")
    }

    private val mockMetadata = MockSrvaMetadata.METADATA_SPEC_VERSION_2.deserializeFromJson<SrvaMetadataDTO>()!!.toSrvaMetadata()

    private fun getProvider(
        metadataRepository: MetadataRepository = MockMetadataRepository(),
        fetchedMetadata: SrvaMetadata = mockMetadata,
        metadataFetcher: SrvaMetadataFetcher = MockSrvaMetadataFetcher(fetchedMetadata),
        preferences: Preferences = MockPreferences(),
        localDateTimeProvider: LocalDateTimeProvider = MockDateTimeProvider(),
        currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = preferences,
        ),
        login: Boolean = true
    ): SrvaMetadataProvider {
        if (login) {
            runBlocking {
                currentUserContextProvider.userLoggedIn(MockUserInfo.Pentti.deserializeFromJson()!!)
            }
        }

        return SrvaMetadataProvider(
            metadataRepository = metadataRepository,
            metadataFetcher = metadataFetcher,
            currentUserContextProvider = currentUserContextProvider,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
        )
    }
}

private class MockSrvaMetadataFetcher(
    mockData: SrvaMetadata
) : MockDataFetcher<SrvaMetadata>(mockData), SrvaMetadataFetcher {
    override val metadata: SrvaMetadata?
        get() = fetchedData
}

