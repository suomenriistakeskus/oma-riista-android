@file:Suppress("ComplexRedundantLet")

package fi.riista.common.domain.srva.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.SrvaContext
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.CommonSrvaSpecimen
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.domain.srva.model.toCommonSrvaMethod
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListCommonSrvaEventsControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, srvaEventYears.size)
            assertEquals(MockMetadataProvider.INSTANCE.srvaMetadata.species + Species.Other, srvaSpecies)
            assertNull(filterYear)
            assertNull(filterSpecies)
            assertFalse(filteringEnabled)
            assertEquals(0, filteredSrvaEvents.size)
        }
    }

    @Test
    fun testListingAllEvents() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredSrvaEvents.size)
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            // todo: the order of srva events should be guaranteed
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 3L }, "remoteId == 3")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 4L }, "remoteId == 4")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 5L }, "remoteId == 5")
            //assertEquals(2, filteredSrvaEvents[0].remoteId)
            //assertEquals(1, filteredSrvaEvents[1].remoteId)
            //assertEquals(3, filteredSrvaEvents[2].remoteId)
            assertFalse(filteringEnabled)

        }
    }

    @Test
    fun testListingDeletedEvents() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.saveSrvaEvent(
            getSrvaEvent(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                huntingYear = 2021,
            ).copy(deleted = true)
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, filteredSrvaEvents.size)
        }
    }

    @Test
    fun testListingAllEventsWithImages() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext, listOnlySrvaEventsWithImages = true)

        with (srvaContext) {
            saveSrvaEvent(
                getSrvaEvent(
                    remoteId = 1,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    huntingYear = 2021,
                    images = getEntityImages()
                )
            )
            saveSrvaEvent(
                getSrvaEvent(
                    remoteId = 2,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    huntingYear = 2022,
                    images = EntityImages.noImages()
                )
            )
        }

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredSrvaEvents.size)
            // todo: we should consider whether all srva event years are returned if filtering only events that have images
            assertEquals(listOf(2022, 2021), srvaEventYears)

            // todo: the order of srva events should be guaranteed
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 1L }, "remoteId == 1")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByYear() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredSrvaEvents.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        controller.setYearFilter(year = 2022)
        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 3L }, "remoteId == 3")
            assertEquals(2022, filterYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterByYearBeforeLoadingViewModel() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()
        controller.setYearFilter(year = 2022)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 3L }, "remoteId == 3")
            assertEquals(2022, filterYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterBySpecies() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredSrvaEvents.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 5L }, "remoteId == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterYear)
        }
    }

    @Test
    fun testFilterBySpeciesBeforeLoadingViewModel() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()
        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 5L }, "remoteId == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterYear)
        }
    }

    @Test
    fun testEmptySpeciesFilter() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredSrvaEvents.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf<Species>()
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredSrvaEvents.size, "filtered after")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByYearAndSpecies() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredSrvaEvents.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setFilters(year = 2022, species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertEquals(2022, filterYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    @Test
    fun testRefreshingFilteredEvents() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val controller = getController(srvaContext = srvaContext)

        srvaContext.addSrvaEvents()

        controller.loadViewModel()

        controller.setYearFilter(year = 2022)
        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertEquals(2022, filterYear)
            assertEquals(speciesFilter, filterSpecies)
        }

        srvaContext.saveSrvaEvent(
            getSrvaEvent(
                remoteId = 6,
                species = speciesFilter[0],
                huntingYear = 2022,
                images = EntityImages.noImages()
            )
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredSrvaEvents.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), srvaEventYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredSrvaEvents.find { it.remoteId == 6L }, "remoteId == 6")
            assertEquals(2022, filterYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    private suspend fun SrvaContext.addSrvaEvents() {
        saveSrvaEvent(
            getSrvaEvent(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                huntingYear = 2021,
            )
        )
        saveSrvaEvent(
            getSrvaEvent(
                remoteId = 2,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                huntingYear = 2022,
            )
        )
        saveSrvaEvent(
            getSrvaEvent(
                remoteId = 3,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                huntingYear = 2022,
            )
        )
        saveSrvaEvent(
            getSrvaEvent(
                remoteId = 4,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                huntingYear = 2020,
            )
        )
        saveSrvaEvent(
            getSrvaEvent(
                remoteId = 5,
                species = Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
                huntingYear = 2020,
            )
        )
    }

    private fun getSrvaEvent(
        remoteId: Long,
        species: Species,
        huntingYear: Int,
        images: EntityImages = EntityImages.noImages()
    ): CommonSrvaEvent {
        return getSrvaEvent(
            remoteId = remoteId,
            species = species,
            pointOfTime = LocalDateTime(huntingYear, 1, 1, 13, 14, 15),
            images = images
        )
    }

    private fun getSrvaEvent(
        remoteId: Long,
        species: Species,
        pointOfTime: LocalDateTime,
        images: EntityImages = EntityImages.noImages()
    ): CommonSrvaEvent {
        return CommonSrvaEvent(
            localId = null,
            localUrl = null,
            remoteId = remoteId,
            revision = 2,
            mobileClientRefId = null,
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = 12,
            canEdit = true,
            modified = false,
            deleted = false,
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = GeoLocationSource.MANUAL.toBackendEnum()
            ),
            pointOfTime = pointOfTime,
            author = CommonSrvaEventAuthor(
                id = 13,
                revision = 14,
                byName = "Pena",
                lastName = "Mujunen"
            ),
            approver = CommonSrvaEventApprover(
                firstName = "Asko",
                lastName = "Partanen"
            ),
            species = species,
            otherSpeciesDescription = null,
            totalSpecimenAmount = 1,
            specimens = listOf(
                CommonSrvaSpecimen(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                )
            ),
            eventCategory = SrvaEventCategoryType.ACCIDENT.toBackendEnum(),
            deportationOrderNumber = null,
            eventType = SrvaEventType.INJURED_ANIMAL.toBackendEnum(),
            otherEventTypeDescription = null,
            eventTypeDetail = BackendEnum.create(null),
            otherEventTypeDetailDescription = null,
            eventResult = SrvaEventResult.ANIMAL_NOT_FOUND.toBackendEnum(),
            eventResultDetail = BackendEnum.create(null),
            methods = listOf(
                SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
                SrvaMethodType.SOUND_EQUIPMENT.toBackendEnum().toCommonSrvaMethod(selected = true),
            ),
            otherMethodDescription = null,
            personCount = 2,
            hoursSpent = 3,
            description = "Käytiin etsimässä, ei löytynyt",
            images = images,
        )
    }

    private fun getEntityImages() = EntityImages(
        remoteImageIds = listOf(),
        localImages = listOf(
            EntityImage(
                serverId = "serverId (not shown)",
                localIdentifier = "localIdentifier (not shown)",
                localUrl = "localUrl (not shown)",
                status = EntityImage.Status.UPLOADED,
            ),
            EntityImage(
                serverId = "serverId",
                localIdentifier = "localIdentifier",
                localUrl = "localUrl",
                status = EntityImage.Status.UPLOADED,
            ),
        )
    )

    private fun getController(
        metadataProvider: MetadataProvider = getMetadataProvider(),
        srvaContext: SrvaContext = getSrvaContext(),
        listOnlySrvaEventsWithImages: Boolean = false
    ): ListCommonSrvaEventsController {
        return ListCommonSrvaEventsController(
            metadataProvider = metadataProvider,
            srvaContext = srvaContext,
            listOnlySrvaEventsWithImages = listOnlySrvaEventsWithImages
        )
    }

    private fun getSrvaContext(): SrvaContext {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val mockBackendAPI = BackendAPIMock()
        val mockUserContextProvider = CurrentUserContextProviderFactory.createMocked()
        val mockDateTimeProvider = MockDateTimeProvider()
        val mockCommonFileProvider = CommonFileProviderMock()

        RiistaSDK.initializeMocked(
            databaseDriverFactory = dbDriverFactory,
            mockBackendAPI = mockBackendAPI,
            mockCurrentUserContextProvider = mockUserContextProvider,
            mockLocalDateTimeProvider = mockDateTimeProvider,
            mockFileProvider = mockCommonFileProvider,
        )

        runBlocking {
            mockUserContextProvider.userLoggedIn(mockUserInfoDTO)
        }

        return SrvaContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = mockBackendAPI
            },
            preferences = MockPreferences(),
            localDateTimeProvider = mockDateTimeProvider,
            commonFileProvider = mockCommonFileProvider,
            database = database,
            currentUserContextProvider = mockUserContextProvider,
        )
    }

    private val mockUserInfoDTO = UserInfoDTO(
        username = "user",
        personId = 123L,
        firstName = "user_first",
        lastName = "user_last",
        birthDate = null,
        address = null,
        homeMunicipality = LocalizedStringDTO(null, null, null),
        rhy = null,
        hunterNumber = null,
        hunterExamDate = null,
        huntingCardStart = null,
        huntingCardEnd = null,
        huntingBanStart = null,
        huntingBanEnd = null,
        huntingCardValidNow = true,
        qrCode = null,
        timestamp = "2022-01-01",
        shootingTests = emptyList(),
        occupations = emptyList(),
        enableSrva = true,
        enableShootingTests = false,
        deerPilotUser = true,
    )

    private fun getMetadataProvider(): MetadataProvider = MockMetadataProvider.INSTANCE
}
