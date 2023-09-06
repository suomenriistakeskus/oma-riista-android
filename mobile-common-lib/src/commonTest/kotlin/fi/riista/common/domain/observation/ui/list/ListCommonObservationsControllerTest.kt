@file:Suppress("ComplexRedundantLet")

package fi.riista.common.domain.observation.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ObservationContext
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
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
import fi.riista.common.model.LocalTime
import fi.riista.common.model.plus
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

class ListCommonObservationsControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, observationHuntingYears.size)
            assertNull(filterHuntingYear)
            assertNull(filterSpecies)
            assertFalse(filteringEnabled)
            assertEquals(0, filteredObservations.size)
        }
    }

    @Test
    fun testListingAllEvents() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredObservations.size)
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            // todo: the order of Observation events should be guaranteed
            assertNotNull(filteredObservations.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 3L }, "remoteId == 3")
            assertNotNull(filteredObservations.find { it.remoteId == 4L }, "remoteId == 4")
            assertNotNull(filteredObservations.find { it.remoteId == 5L }, "remoteId == 5")
            //assertEquals(2, filteredObservations[0].remoteId)
            //assertEquals(1, filteredObservations[1].remoteId)
            //assertEquals(3, filteredObservations[2].remoteId)
            assertFalse(filteringEnabled)

        }
    }

    @Test
    fun testListingDeletedEvents() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.saveObservation(
            getObservation(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2021, 12),
            ).copy(deleted = true)
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, filteredObservations.size)
        }
    }

    @Test
    fun testListingAllEventsWithImages() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext, listOnlyObservationsWithImages = true)

        with (observationContext) {
            saveObservation(
                getObservation(
                    remoteId = 1,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    pointOfTime = getDatetimeForHuntingYear(2021, 12),
                    images = getEntityImages()
                )
            )
            saveObservation(
                getObservation(
                    remoteId = 2,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    pointOfTime = getDatetimeForHuntingYear(2022, 12),
                    images = EntityImages.noImages()
                )
            )
        }

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredObservations.size)
            // todo: we should consider whether all Observation event years are returned if filtering only events that have images
            assertEquals(listOf(2022, 2021), observationHuntingYears)

            // todo: the order of Observation events should be guaranteed
            assertNotNull(filteredObservations.find { it.remoteId == 1L }, "remoteId == 1")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByHuntingYear() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredObservations.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        controller.setHuntingYearFilter(huntingYear = 2022)
        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 3L }, "remoteId == 3")
            assertEquals(2022, filterHuntingYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterByHuntingYearBeforeLoadingViewModel() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()
        controller.setHuntingYearFilter(huntingYear = 2022)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 3L }, "remoteId == 3")
            assertEquals(2022, filterHuntingYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterBySpecies() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredObservations.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 5L }, "remoteId == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterHuntingYear)
        }
    }

    @Test
    fun testFilterBySpeciesBeforeLoadingViewModel() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()
        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 1L }, "remoteId == 1")
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 5L }, "remoteId == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterHuntingYear)
        }
    }

    @Test
    fun testEmptySpeciesFilter() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredObservations.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf<Species>()
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredObservations.size, "filtered after")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByHuntingYearAndSpecies() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredObservations.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setFilters(huntingYear = 2022, species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    @Test
    fun testRefreshingFilteredEvents() = runBlockingTest {
        val observationContext = getObservationContext()
        val controller = getController(observationContext = observationContext)

        observationContext.addObservations()

        controller.loadViewModel()

        controller.setHuntingYearFilter(huntingYear = 2022)
        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }

        observationContext.saveObservation(
            getObservation(
                remoteId = 6,
                species = speciesFilter[0],
                pointOfTime = getDatetimeForHuntingYear(2022, 12),
                images = EntityImages.noImages()
            )
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredObservations.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), observationHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredObservations.find { it.remoteId == 2L }, "remoteId == 2")
            assertNotNull(filteredObservations.find { it.remoteId == 6L }, "remoteId == 6")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    private suspend fun ObservationContext.addObservations() {
        saveObservation(
            getObservation(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2021, 12),
            )
        )
        saveObservation(
            getObservation(
                remoteId = 2,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2022, 12),
            )
        )
        saveObservation(
            getObservation(
                remoteId = 3,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                pointOfTime = getDatetimeForHuntingYear(2022, 251),
            )
        )
        saveObservation(
            getObservation(
                remoteId = 4,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                pointOfTime = getDatetimeForHuntingYear(2020, 3),
            )
        )
        saveObservation(
            getObservation(
                remoteId = 5,
                species = Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
                pointOfTime = getDatetimeForHuntingYear(2020, 174),
            )
        )
    }

    private fun getDatetimeForHuntingYear(huntingYear: Int, daysFromHuntingYearStart: Int) : LocalDateTime {
        val startOfTheHuntingYear = LocalDateTime(
            date = Constants.FIRST_DATE_OF_HUNTING_YEAR.toLocalDate(huntingYear),
            time = LocalTime(12, 0, 0) // probably starts already at 00:00 but let's start at noon
        )

        return startOfTheHuntingYear.plus(days = daysFromHuntingYearStart)
    }

    private fun getObservation(
        remoteId: Long,
        species: Species,
        pointOfTime: LocalDateTime,
        images: EntityImages = EntityImages.noImages()
    ): CommonObservation {
        return CommonObservation(
            localId = null,
            localUrl = null,
            remoteId = remoteId,
            revision = 2,
            mobileClientRefId = null,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = species,
            observationCategory = ObservationCategory.NORMAL.toBackendEnum(),
            observationType = ObservationType.NAKO.toBackendEnum(),
            deerHuntingType = BackendEnum.create(null),
            deerHuntingOtherTypeDescription = null,
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = GeoLocationSource.MANUAL.toBackendEnum()
            ),
            pointOfTime = pointOfTime,
            description = "tämmönen tuli vastaan",
            images = images,
            totalSpecimenAmount = 1,
            specimens = listOf(
                CommonObservationSpecimen(
                    remoteId = 1L,
                    revision = 1,
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = BackendEnum.create(null),
                    marking = BackendEnum.create(null),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                )
            ),
            canEdit = true,
            modified = false,
            deleted = false,
            mooselikeMaleAmount = null,
            mooselikeFemaleAmount = null,
            mooselikeFemale1CalfAmount = null,
            mooselikeFemale2CalfsAmount = null,
            mooselikeFemale3CalfsAmount = null,
            mooselikeFemale4CalfsAmount = null,
            mooselikeCalfAmount = null,
            mooselikeUnknownSpecimenAmount = null,
            observerName = null,
            observerPhoneNumber = null,
            officialAdditionalInfo = null,
            verifiedByCarnivoreAuthority = null,
            inYardDistanceToResidence = null,
            litter = null,
            pack = null,
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
        observationContext: ObservationContext = getObservationContext(),
        listOnlyObservationsWithImages: Boolean = false
    ): ListCommonObservationsController {
        return ListCommonObservationsController(
            metadataProvider = metadataProvider,
            observationContext = observationContext,
            listOnlyObservationsWithImages = listOnlyObservationsWithImages
        )
    }

    private fun getObservationContext(): ObservationContext {
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

        return ObservationContext(
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
