package fi.riista.common.domain.harvest.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.io.CommonFileProviderMock
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

class ListCommonHarvestsControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() {
        val controller = getController()

        runBlocking {
            controller.loadViewModel()
        }

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, harvestHuntingYears.size)
            assertNull(filterHuntingYear)
            assertNull(filterSpecies)
            assertFalse(filteringEnabled)
            assertEquals(0, filteredHarvests.size)
        }
    }

    @Test
    fun testListingAllEvents() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredHarvests.size)
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            // todo: the order of Harvest events should be guaranteed
            assertNotNull(filteredHarvests.find { it.id == 1L }, "id == 1")
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 3L }, "id == 3")
            assertNotNull(filteredHarvests.find { it.id == 4L }, "id == 4")
            assertNotNull(filteredHarvests.find { it.id == 5L }, "id == 5")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testListingDeletedEvents() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.saveHarvest(
            getHarvest(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2021, 12),
            ).copy(deleted = true)
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(0, filteredHarvests.size)
        }
    }

    @Test
    fun testListingAllEventsWithImages() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext, listOnlyHarvestsWithImages = true)

        with (harvestContext) {
            saveHarvest(
                getHarvest(
                    remoteId = 1,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    pointOfTime = getDatetimeForHuntingYear(2021, 12),
                    images = getEntityImages()
                )
            )
            saveHarvest(
                getHarvest(
                    remoteId = 2,
                    species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                    pointOfTime = getDatetimeForHuntingYear(2022, 12),
                    images = EntityImages.noImages()
                )
            )
        }

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredHarvests.size)
            // todo: we should consider whether all Harvest event years are returned if filtering only events that have images
            assertEquals(listOf(2022, 2021), harvestHuntingYears)

            // todo: the order of Harvest events should be guaranteed
            assertNotNull(filteredHarvests.find { it.id == 1L }, "id == 1")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByHuntingYear() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        controller.setHuntingYearFilter(huntingYear = 2022)
        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 3L }, "id == 3")
            assertEquals(2022, filterHuntingYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterByHuntingYearBeforeLoadingViewModel() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()
        controller.setHuntingYearFilter(huntingYear = 2022)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 3L }, "id == 3")
            assertEquals(2022, filterHuntingYear)
            assertNull(filterSpecies)
        }
    }

    @Test
    fun testFilterBySpecies() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 1L }, "id == 1")
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 5L }, "id == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterHuntingYear)
        }
    }

    @Test
    fun testFilterBySpeciesBeforeLoadingViewModel() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()
        val speciesFilter = listOf(
            Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
        )
        controller.setSpeciesFilter(species = speciesFilter)

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(3, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 1L }, "id == 1")
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 5L }, "id == 5")
            assertEquals(speciesFilter, filterSpecies)
            assertNull(filterHuntingYear)
        }
    }

    @Test
    fun testEmptySpeciesFilter() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf<Species>()
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredHarvests.size, "filtered after")
            assertFalse(filteringEnabled)
        }
    }

    @Test
    fun testFilterByHuntingYearAndSpecies() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setFilters(ownHarvests = true, huntingYear = 2022, species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    @Test
    fun testFilterOwnHarvests() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        controller.setOwnHarvestsFilter(ownHarvests = true)
        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(5, filteredHarvests.size, "filtered after")
        }
    }

    @Test
    fun testFilterHarvestsForOthers() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel 1")) {
            assertEquals(5, filteredHarvests.size, "filtered before")
            assertFalse(filteringEnabled)
        }

        controller.setOwnHarvestsFilter(ownHarvests = false)
        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredHarvests.size, "filtered after")
            assertEquals(filteredHarvests[0].actorInfo.personWithHunterNumber?.id, 123)
            assertEquals(filteredHarvests[0].actorInfo.personWithHunterNumber?.rev, 1)
            assertEquals(filteredHarvests[0].actorInfo.personWithHunterNumber?.byName, "Anssi")
            assertEquals(filteredHarvests[0].actorInfo.personWithHunterNumber?.lastName, "Aaltonen")
            assertEquals(filteredHarvests[0].actorInfo.personWithHunterNumber?.hunterNumber, "22222222")
        }
    }

    @Test
    fun testRefreshingFilteredEvents() = runBlockingTest {
        val harvestContext = getHarvestContext()
        val controller = getController(harvestContext = harvestContext)

        harvestContext.addHarvests()

        controller.loadViewModel()

        controller.setHuntingYearFilter(huntingYear = 2022)
        val speciesFilter = listOf(Species.Known(speciesCode = SpeciesCodes.MOOSE_ID))
        controller.setSpeciesFilter(species = speciesFilter)

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(1, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }

        harvestContext.saveHarvest(
            getHarvest(
                remoteId = 6,
                species = speciesFilter[0],
                pointOfTime = getDatetimeForHuntingYear(2022, 12),
                images = EntityImages.noImages()
            )
        )

        controller.loadViewModel()

        with (assertNotNull(controller.getLoadedViewModelOrNull(), "viewModel")) {
            assertEquals(2, filteredHarvests.size, "filtered after")
            assertEquals(listOf(2022, 2021, 2020), harvestHuntingYears)

            assertTrue(filteringEnabled)
            assertNotNull(filteredHarvests.find { it.id == 2L }, "id == 2")
            assertNotNull(filteredHarvests.find { it.id == 6L }, "id == 6")
            assertEquals(2022, filterHuntingYear)
            assertEquals(speciesFilter, filterSpecies)
        }
    }

    private suspend fun HarvestContext.addHarvests() {
        saveHarvest(
            getHarvest(
                remoteId = 1,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2021, 12),
            )
        )
        saveHarvest(
            getHarvest(
                remoteId = 2,
                species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
                pointOfTime = getDatetimeForHuntingYear(2022, 12),
            )
        )
        saveHarvest(
            getHarvest(
                remoteId = 3,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                pointOfTime = getDatetimeForHuntingYear(2022, 251),
            )
        )
        saveHarvest(
            getHarvest(
                remoteId = 4,
                species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
                pointOfTime = getDatetimeForHuntingYear(2020, 3),
            )
        )
        saveHarvest(
            getHarvest(
                remoteId = 5,
                species = Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
                pointOfTime = getDatetimeForHuntingYear(2020, 174),
            )
        )
        saveHarvest(
            getHarvest(
                remoteId = 6,
                species = Species.Known(speciesCode = SpeciesCodes.BEAR_ID),
                pointOfTime = getDatetimeForHuntingYear(2022, 12),
                actor = GroupHuntingPerson.Guest(PersonWithHunterNumber(
                    id = 123,
                    rev = 1,
                    byName = "Anssi",
                    lastName = "Aaltonen",
                    hunterNumber = "22222222",
                ))
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

    private fun getHarvest(
        remoteId: Long,
        species: Species,
        pointOfTime: LocalDateTime,
        images: EntityImages = EntityImages.noImages(),
        actor: GroupHuntingPerson = GroupHuntingPerson.Unknown,
    ): CommonHarvest {
        return CommonHarvest(
            localId = null,
            localUrl = null,
            id = remoteId,
            rev = 2,
            mobileClientRefId = null,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            species = species,
            deerHuntingType = BackendEnum.create(null),
            deerHuntingOtherTypeDescription = null,
            geoLocation = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = GeoLocationSource.MANUAL.toBackendEnum()
            ),
            pointOfTime = pointOfTime,
            description = "tämmönen tuli vastaan",
            images = images,
            amount = 1,
            specimens = listOf(
                CommonHarvestSpecimen(
                    id = 1L,
                    rev = 1,
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    weight = null,
                    antlersLost = null,
                    notEdible = null,
                    alone = null,
                    weightEstimated = null,
                    weightMeasured = null,
                    fitnessClass = BackendEnum.create(null),
                    antlersType = BackendEnum.create(null),
                    antlersWidth = null,
                    antlerPointsLeft = null,
                    antlerPointsRight = null,
                    antlersGirth = null,
                    antlersLength = null,
                    antlersInnerWidth = null,
                    antlerShaftWidth = null,
                    additionalInfo = null,
                )
            ),
            canEdit = true,
            modified = false,
            deleted = false,
            feedingPlace = false,
            greySealHuntingMethod = null.toBackendEnum(),
            harvestReportDone = true,
            harvestReportRequired = true,
            harvestReportState = HarvestReportState.SENT_FOR_APPROVAL.toBackendEnum(),
            permitNumber = null,
            permitType = null,
            rejected = false,
            stateAcceptedToHarvestPermit = null.toBackendEnum(),
            taigaBeanGoose = false,
            actorInfo = actor,
            selectedClub = null,
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
        harvestContext: HarvestContext = getHarvestContext(),
        listOnlyHarvestsWithImages: Boolean = false
    ): ListCommonHarvestsController {
        return ListCommonHarvestsController(
            harvestContext = harvestContext,
            listOnlyHarvestsWithImages = listOnlyHarvestsWithImages
        )
    }

    private fun getHarvestContext(): HarvestContext {
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
        )

        runBlocking {
            mockUserContextProvider.userLoggedIn(mockUserInfoDTO)
        }

        return HarvestContext(
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
}
