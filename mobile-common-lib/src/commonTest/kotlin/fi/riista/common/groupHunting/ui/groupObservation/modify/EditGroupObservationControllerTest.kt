package fi.riista.common.groupHunting.ui.groupObservation.modify

import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.groupHunting.MockGroupHuntingData
import fi.riista.common.groupHunting.model.GroupHuntingObservationTarget
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.helpers.*
import fi.riista.common.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.userInfo.CurrentUserContextProviderFactory
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
            )
            assertEquals(location, it.location)
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesCodeField(expectedIndex++, GroupObservationField.SPECIES_CODE).let {
            assertEquals(SpeciesCodes.MOOSE_ID, it.speciesCode)
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

    private fun getStringProvider(): StringProvider {
        return object : StringProvider {
            @Suppress("SpellCheckingInspection")
            override fun getString(stringId: RStringId): String {
                return when (stringId) {
                    RR.string.group_hunting_observation_field_actor -> "actor"
                    RR.string.group_hunting_observation_field_observation_type -> "observation_type"
                    RR.string.observation_type_nako -> "nako"
                    RR.string.group_hunting_observation_field_headline_specimen_details -> "specimen_details"
                    RR.string.group_hunting_observation_field_mooselike_male_amount -> "male_amount"
                    RR.string.group_hunting_observation_field_mooselike_female_amount -> "female_amount"
                    RR.string.group_hunting_observation_field_mooselike_female_1calf_amount -> "female_1calf_amount"
                    RR.string.group_hunting_observation_field_mooselike_female_2calf_amount -> "female_2calf_amount"
                    RR.string.group_hunting_observation_field_mooselike_female_3calf_amount -> "female_3calf_amount"
                    RR.string.group_hunting_observation_field_mooselike_female_4calf_amount -> "female_4calf_amount"
                    RR.string.group_hunting_observation_field_mooselike_calf_amount -> "calf_amount"
                    RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount -> "unknown_specimen_amount"
                    RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting ->
                        "male_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting ->
                        "female_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting ->
                        "female_1calf_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting ->
                        "female_2calf_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting ->
                        "female_3calf_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting ->
                        "female_4calf_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting ->
                        "calf_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting ->
                        "unknown_specimen_amount_within_deer_hunting"
                    RR.string.group_hunting_observation_field_hunting_day_and_time -> "hunting_day_and_time"
                    RR.string.group_hunting_other_observer -> "other"
                    RR.string.group_hunting_hunter_id -> "group_hunting_hunter_id"
                    RR.string.group_hunting_proposed_group_harvest_actor -> "harvest_actor"
                    RR.string.group_member_selection_select_hunter -> "select_hunter"
                    RR.string.group_member_selection_select_observer -> "select_observer"
                    RR.string.group_member_selection_search_by_name -> "search_by_name"
                    RR.string.group_member_selection_name_hint -> "name_hint"
                    else -> {
                        throw RuntimeException("Unexpected stringId ($stringId) requested")
                    }
                }
            }

            override fun getFormattedString(stringId: RStringId, arg: String): String {
                throw RuntimeException("Unexpected stringId ($stringId) requested")
            }
        }
    }
}

private fun EditGroupObservationController.getLoadedViewModel(): ModifyGroupObservationViewModel {
    return (viewModelLoadStatus.value as ViewModelLoadStatus.Loaded).viewModel
}
