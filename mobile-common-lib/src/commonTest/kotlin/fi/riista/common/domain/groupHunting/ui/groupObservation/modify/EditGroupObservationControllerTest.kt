package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationUpdateDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationTarget
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.helpers.*
import fi.riista.common.domain.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import kotlin.test.*

class EditGroupObservationControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        assertNotNull(controller.getLoadedViewModel().observation)
    }

    @Test
    fun testMooseCantBeAcceptedWithoutHuntingDayId() = runBlockingTest {
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()
        val response = controller.acceptObservation()
        assertTrue(response is GroupHuntingObservationOperationResponse.Error)
    }

    @Test
    fun testMooseCanBeAccepted() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(backendAPI),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        controller.huntingDayEventDispatcher.dispatchHuntingDayChanged(
            GroupObservationField.HUNTING_DAY_AND_TIME,
            MockGroupHuntingData.FirstHuntingDayId
        )

        val response = controller.acceptObservation()
        assertTrue(response is GroupHuntingObservationOperationResponse.Success)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        val fields = viewModel.fields
        assertEquals(14, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, GroupObservationField.LOCATION).let {
            val location = ETRMSGeoLocation(
                latitude = 6789568,
                longitude = 330224,
                source = BackendEnum.create(GeoLocationSource.MANUAL),
                accuracy =  1.2,
                altitude = null,
                altitudeAccuracy = null,
            ).asKnownLocation()
            assertEquals(location, it.location)
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, GroupObservationField.SPECIES_CODE).let {
            assertEquals(Species.Known(SpeciesCodes.MOOSE_ID), it.species)
            assertTrue(it.settings.readOnly)
        }
        fields.getHuntingDayAndTimeField(expectedIndex++, GroupObservationField.HUNTING_DAY_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 10, 13, 0, 0), it.dateAndTime)
            assertFalse(it.settings.readOnly)
            assertEquals("hunting_day_and_time", it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.OBSERVATION_TYPE).let {
            assertEquals("nako", it.value)
            assertEquals("observation_type", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, GroupObservationField.ACTOR).let {
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.ACTOR_HUNTER_NUMBER).let {
            assertTrue(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, GroupObservationField.HEADLINE_SPECIMEN_DETAILS).let {
            assertEquals("specimen_details", it.text)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_MALE_AMOUNT).let {
            assertEquals(0, it.value)
            assertEquals("male_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_AMOUNT).let {
            assertEquals(1, it.value)
            assertEquals("female_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT).let {
            assertEquals(3, it.value)
            assertEquals("female_1calf_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT).let {
            assertEquals(4, it.value)
            assertEquals("female_2calf_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT).let {
            assertEquals(5, it.value)
            assertEquals("female_3calf_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_CALF_AMOUNT).let {
            assertEquals(2, it.value)
            assertEquals("calf_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT).let {
            assertEquals(6, it.value)
            assertEquals("unknown_specimen_amount", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testAcceptingObservationSavesItWithLatestSpecVersion() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = EditGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(backendApi = backendAPIMock),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertEquals(3, controller.getLoadedViewModelOrNull()?.observation?.observationSpecVersion)

        controller.huntingDayEventDispatcher.dispatchHuntingDayChanged(
            GroupObservationField.HUNTING_DAY_AND_TIME,
            MockGroupHuntingData.FirstHuntingDayId
        )

        val response = controller.acceptObservation()
        assertTrue(response is GroupHuntingObservationOperationResponse.Success)
        val callParameter = backendAPIMock.callParameter(BackendAPIMock::updateGroupHuntingObservation.name)
        val dto = callParameter as GroupHuntingObservationUpdateDTO
        assertEquals(Constants.OBSERVATION_SPEC_VERSION, dto.observationSpecVersion)
    }

    private fun getGroupHuntingContext(backendApi: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendApi,
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getObservationTarget(observationId: Long = MockGroupHuntingData.FirstObservationId): GroupHuntingObservationTarget {
        return GroupHuntingObservationTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = 344,
            observationId = observationId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
}

private fun EditGroupObservationController.getLoadedViewModel(): ModifyGroupObservationViewModel {
    return (viewModelLoadStatus.value as ViewModelLoadStatus.Loaded).viewModel
}
