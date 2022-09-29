package fi.riista.common.domain.groupHunting.ui.groupHarvest.view

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestTarget
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.helpers.*
import fi.riista.common.domain.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import kotlin.test.*

class ViewGroupHarvestControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = ViewGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = ViewGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadHarvest()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val harvest = viewModel.harvestData
        assertEquals(MockGroupHuntingData.FirstHarvestId, harvest.id)
        assertEquals(2, harvest.rev)
        // rest of the fields tested in GroupHuntingDiaryProviderTest
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val controller = ViewGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                harvestTarget = getHarvestTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadHarvest()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(17, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, GroupHarvestField.LOCATION).let {
            val location = ETRMSGeoLocation(
                    latitude = 6820960,
                    longitude = 318112,
                    source = BackendEnum.create(GeoLocationSource.MANUAL),
                    accuracy = 0.0,
                    altitude = null,
                    altitudeAccuracy = null,
            ).asKnownLocation()
            assertEquals(location, it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, GroupHarvestField.SPECIES_CODE).let {
            assertEquals(Species.Known(SpeciesCodes.MOOSE_ID), it.species)
            assertTrue(it.settings.readOnly)
        }
        fields.getDateTimeField(expectedIndex++, GroupHarvestField.DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 14, 0, 0), it.dateAndTime)
            assertTrue(it.settings.readOnly)
            assertNull(it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ACTOR).let {
            assertEquals("Pentti Makunen", it.value)
            assertEquals("actor", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.AUTHOR).let {
            assertEquals("Pena Mujunen", it.value)
            assertEquals("author", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getGenderField(expectedIndex++, GroupHarvestField.GENDER).let {
            assertEquals(Gender.MALE, it.gender)
            assertTrue(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, GroupHarvestField.AGE).let {
            assertEquals(GameAge.ADULT, it.age)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.NOT_EDIBLE).let {
            assertEquals("no", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("not_edible", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.WEIGHT_ESTIMATED).let {
            assertEquals("34.0", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("weight_estimated", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.WEIGHT_MEASURED).let {
            assertEquals("4.0", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("weight_measured", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.FITNESS_CLASS).let {
            assertEquals("fitness_class_naantynyt", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("fitness_class", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ANTLERS_LOST).let {
            assertEquals("no", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("antlers_lost", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ANTLERS_TYPE).let {
            assertEquals("antler_type_hanko", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("antlers_type", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ANTLERS_WIDTH).let {
            assertEquals("24", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("antlers_width", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ANTLER_POINTS_LEFT).let {
            assertEquals("4", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("antler_points_left", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupHarvestField.ANTLER_POINTS_RIGHT).let {
            assertEquals("1", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("antler_points_right", it.settings.label)
        }
        fields.getStringField(expectedIndex, GroupHarvestField.ADDITIONAL_INFORMATION).let {
            assertEquals("additional_info", it.value)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
            assertEquals("additional_information", it.settings.label)
        }
    }

    @Test
    fun testHarvestActionsForApprovedHarvest() = runBlockingTest {
        val controller = ViewGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadHarvest()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertTrue(viewModel.canEditHarvest)
        assertFalse(viewModel.canApproveHarvest)
        assertTrue(viewModel.canRejectHarvest)
    }

    @Test
    fun testHarvestActionsForUnapprovedHarvest() = runBlockingTest {
        val controller = ViewGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(MockGroupHuntingData.SecondHarvestId),
            stringProvider = getStringProvider()
        )

        controller.loadHarvest()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertFalse(viewModel.canEditHarvest)
        assertTrue(viewModel.canApproveHarvest)
        assertTrue(viewModel.canRejectHarvest)
    }

    @Test
    fun testHarvestActionsWhenDiaryCantBeEdited() = runBlockingTest {
        val backendApi = BackendAPIMock(
            groupHuntingGroupStatusResponse = MockResponse.success(MockGroupHuntingData.GroupStatusCantEditOrCreate),
        )
        val controller = ViewGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendApi),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )
        controller.loadHarvest()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertFalse(viewModel.canEditHarvest)
        assertFalse(viewModel.canApproveHarvest)
        assertFalse(viewModel.canRejectHarvest)

    }

    private fun getGroupHuntingContext(backendApi: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendApi,
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getHarvestTarget(harvestId: Long = MockGroupHuntingData.FirstHarvestId): GroupHuntingHarvestTarget {
        return GroupHuntingHarvestTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = 344,
            harvestId = harvestId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

}
