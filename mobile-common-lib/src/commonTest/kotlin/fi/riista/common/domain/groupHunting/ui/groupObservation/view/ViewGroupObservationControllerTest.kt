package fi.riista.common.domain.groupHunting.ui.groupObservation.view

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationTarget
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
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

class ViewGroupObservationControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val observation = viewModel.observationData
        assertEquals(MockGroupHuntingData.FirstObservationId, observation.id)
        assertEquals(1, observation.rev)
        // rest of the fields tested in GroupHuntingDiaryProviderTest
    }

    @Test
    fun testMooseProducedFieldsMatchData() = runBlockingTest {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(14, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, GroupObservationField.LOCATION).let {
            val location = ETRMSGeoLocation(
                latitude = 6789568,
                longitude = 330224,
                source = BackendEnum.create(GeoLocationSource.MANUAL),
                accuracy = 1.2,
                altitude = null,
                altitudeAccuracy = null,
            ).asKnownLocation()
            assertEquals(location, it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, GroupObservationField.SPECIES_CODE).let {
            assertEquals(Species.Known(SpeciesCodes.MOOSE_ID), it.species)
            assertTrue(it.settings.readOnly)
        }
        fields.getDateTimeField(expectedIndex++, GroupObservationField.DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 10, 13, 0, 0), it.dateAndTime)
            assertTrue(it.settings.readOnly)
            assertNull(it.settings.label)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.OBSERVATION_TYPE).let {
            assertEquals("nako", it.value)
            assertEquals("observation_type", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.ACTOR).let {
            assertEquals("Pentti Makunen", it.value)
            assertEquals("actor", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.AUTHOR).let {
            assertEquals("Pena Mujunen", it.value)
            assertEquals("author", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, GroupObservationField.HEADLINE_SPECIMEN_DETAILS).let {
            assertEquals("specimen_details", it.text)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_MALE_AMOUNT).let {
            assertEquals("0", it.value)
            assertEquals("male_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_AMOUNT).let {
            assertEquals("1", it.value)
            assertEquals("female_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT).let {
            assertEquals("3", it.value)
            assertEquals("female_1calf_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT).let {
            assertEquals("4", it.value)
            assertEquals("female_2calf_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT).let {
            assertEquals("5", it.value)
            assertEquals("female_3calf_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, GroupObservationField.MOOSELIKE_CALF_AMOUNT).let {
            assertEquals("2", it.value)
            assertEquals("calf_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT).let {
            assertEquals("6", it.value)
            assertEquals("unknown_specimen_amount", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testWhiteTailedDeerProducedFieldsMatchData() = runBlockingTest {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(observationId = MockGroupHuntingData.SecondObservationId),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(15, fields.size)

        // Fields that are same with moose are not repeated here, test just extra fields
        fields.getStringField(12, GroupObservationField.MOOSELIKE_FEMALE_4CALF_AMOUNT).let {
            assertEquals("7", it.value)
            assertEquals("female_4calf_amount_within_deer_hunting", it.settings.label)
            assertTrue(it.settings.singleLine)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testObservationActionsForApprovedObservation() = runBlockingTest {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertTrue(viewModel.canRejectObservation)
    }

    @Test
    fun testObservationActionsForUnapprovedObservation() = runBlockingTest {
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(),
            observationTarget = getObservationTarget(MockGroupHuntingData.SecondObservationId),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertTrue(viewModel.canRejectObservation)
    }

    @Test
    fun testObservationActionsWhenDiaryCantBeEdited() = runBlockingTest {
        val backendApi = BackendAPIMock(
            groupHuntingGroupStatusResponse = MockResponse.success(MockGroupHuntingData.GroupStatusCantEditOrCreate),
        )
        val controller = ViewGroupObservationController(
            groupHuntingContext = getGroupHuntingContext(backendApi = backendApi),
            observationTarget = getObservationTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadObservation()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertFalse(viewModel.canRejectObservation)
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
