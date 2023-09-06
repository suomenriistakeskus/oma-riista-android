package fi.riista.common.domain.observation.ui.view

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.observation.ObservationContext
import fi.riista.common.domain.observation.ObservationOperationResponse
import fi.riista.common.domain.observation.metadata.model.ObservationFieldRequirement
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.metadata.model.SpeciesObservationMetadata
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.getField
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.io.CommonFileProviderMock
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
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewObservationControllerTest {

    // always present fields

    @Test
    fun testAlwaysPresentFieldCount() = runBlockingTest {
        val controller = getController()
        controller.loadViewModel()

        with (controller.getLoadedViewModel()) {
            assertEquals(5, fields.size)
        }
    }

    @Test
    fun testMapIsAlwaysPresent() = runBlockingTest {
        assertFieldExists<LocationField<CommonObservationField>>(
            controller = getController(),
            expectedIndex = 0,
            fieldId = CommonObservationField.LOCATION
        ) { field ->
            assertEquals(OBSERVATION_LOCATION.asKnownLocation(), field.location)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCodeIsAlwaysPresent() = runBlockingTest {
        assertFieldExists<SpeciesField<CommonObservationField>>(
            controller = getController(),
            expectedIndex = 1,
            fieldId = CommonObservationField.SPECIES_AND_IMAGE
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.species)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testDateTimeIsAlwaysPresent() = runBlockingTest {
        assertFieldExists<DateAndTimeField<CommonObservationField>>(
            controller = getController(),
            expectedIndex = 2,
            fieldId = CommonObservationField.DATE_AND_TIME
        ) { field ->
            assertEquals(OBSERVATION_DATE_TIME, field.dateAndTime)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testObservationTypeIsAlwaysPresent() = runBlockingTest {
        assertFieldExists<StringField<CommonObservationField>>(
            controller = getController(),
            expectedIndex = 3,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        ) { field ->
            field.value.assertEquals(ObservationType.NAKO.resourcesStringId)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_observation_type)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testDescriptionIsAlwaysPresent() = runBlockingTest {
        assertFieldExists<StringField<CommonObservationField>>(
            controller = getController(),
            expectedIndex = 4,
            fieldId = CommonObservationField.DESCRIPTION
        ) { field ->
            assertEquals("description", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_description)
            assertTrue(field.settings.readOnly)
        }
    }


    // fields depending on metadata

    @Test
    fun testObservationCategory() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.OBSERVATION_CATEGORY to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.OBSERVATION_CATEGORY
        ) { field ->
            field.value.assertEquals(ObservationCategory.NORMAL.resourcesStringId)
            field.settings.label.assertEquals(RR.string.observation_label_observation_category)
            assertTrue(field.settings.readOnly)
        }

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        )
    }

    @Test
    fun testWithinMooseHunting() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.WITHIN_MOOSE_HUNTING to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.WITHIN_MOOSE_HUNTING
        ) { field ->
            field.value.assertEquals(RR.string.generic_no)
            field.settings.label.assertEquals(RR.string.observation_label_within_moose_hunting)
            assertTrue(field.settings.readOnly)
        }

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        )
    }

    @Test
    fun testWithinDeerHunting() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.WITHIN_DEER_HUNTING to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.WITHIN_DEER_HUNTING
        ) { field ->
            field.value.assertEquals(RR.string.generic_no)
            field.settings.label.assertEquals(RR.string.observation_label_within_deer_hunting)
            assertTrue(field.settings.readOnly)
        }

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        )
    }

    @Test
    fun testDeerHuntingType() = runBlockingTest {
        val controller = getController(
            metadata = createMetadata(
                dynamicObservationFieldRequirement = CommonObservationField.DEER_HUNTING_TYPE to ObservationFieldRequirement.VOLUNTARY,
            )
        )
        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.DEER_HUNTING_TYPE
        ) { field ->
            field.value.assertEquals(RR.string.deer_hunting_type_other)
            field.settings.label.assertEquals(RR.string.harvest_label_deer_hunting_type)
            assertTrue(field.settings.readOnly)
        }

        assertFieldDoesntExist(controller, CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION)
    }

    @Test
    fun testDeerHuntingOtherTypeDescription() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION
        ) { field ->
            assertEquals("deerHuntingOtherTypeDescription", field.value)
            field.settings.label.assertEquals(RR.string.harvest_label_deer_hunting_other_type_description)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testAmountCausesSpecimensToBeDisplayed() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.SPECIMEN_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.SPECIMEN_AMOUNT
        ) { field ->
            assertEquals("1", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_amount)
            assertTrue(field.settings.readOnly)
        }

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 5,
            fieldId = CommonObservationField.DESCRIPTION
        )

        // as the last item
        assertFieldExists<SpecimenField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 6,
            fieldId = CommonObservationField.SPECIMENS
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.specimenData.species)
            assertEquals(1, field.specimenData.specimens.size)
        }
    }

    @Test
    fun testTotalSpecimenAmountDoesNotNeedToMatchActualSpecimenCount() = runBlockingTest {
        val observation = createObservation().copy(
            totalSpecimenAmount = 2,
        )

        assertEquals(2, observation.totalSpecimenAmount, "observation")
        assertEquals(1, observation.specimens?.size, "observation")

        val controller = getController(
            observation =  observation,
            metadata = createMetadata(
                dynamicObservationFieldRequirement = CommonObservationField.SPECIMEN_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
            )
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        assertEquals(2, viewModel.observation.totalSpecimenAmount, "vm observation")
        assertEquals(1, viewModel.observation.specimens?.size, "vm observation")

        assertFieldExists<SpecimenField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 6,
            fieldId = CommonObservationField.SPECIMENS
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.specimenData.species)
            assertEquals(2, field.specimenData.specimenAmount)
            assertEquals(1, field.specimenData.specimens.size)
        }
    }

    @Test
    fun testMooselikeMaleAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_MALE_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_MALE_AMOUNT
        ) { field ->
            assertEquals("2", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_male_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemaleAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT
        ) { field ->
            assertEquals("3", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale1CalfAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT
        ) { field ->
            assertEquals("4", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_1calf_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale2CalfsAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT
        ) { field ->
            assertEquals("5", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_2calf_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale3CalfsAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT
        ) { field ->
            assertEquals("6", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_3calf_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale4CalfsAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT
        ) { field ->
            assertEquals("7", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_4calf_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeCalfAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_CALF_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_CALF_AMOUNT
        ) { field ->
            assertEquals("8", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_calf_amount)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeUnknownSpecimenAmount() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT
        ) { field ->
            assertEquals("9", field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount)
            assertTrue(field.settings.readOnly)
        }
    }



    // Tassu fields

    @Test
    fun testVerifiedByCarnivoreAuthority() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY
        ) { field ->
            field.value.assertEquals(RR.string.generic_yes)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_verified_by_carnivore_authority)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testObserverName() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.TASSU_OBSERVER_NAME to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_OBSERVER_NAME
        ) { field ->
            assertEquals("observerName", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_observer_name)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testObserverPhoneNumber() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.TASSU_OBSERVER_PHONENUMBER to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_OBSERVER_PHONENUMBER
        ) { field ->
            assertEquals("observerPhoneNumber", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_observer_phonenumber)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testOfficialAdditionalInfo() = runBlockingTest {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO
        ) { field ->
            assertEquals("officialAdditionalInfo", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_official_additional_information)
            assertTrue(field.settings.readOnly)
        }
    }


    // Tassu fields, readonly, depending on current observation values

    @Test
    fun testInYardDistanceToResidence() = runBlockingTest {
        assertFieldExists<StringField<CommonObservationField>>(
            controller = getController(observation = createObservation().copy(
                inYardDistanceToResidence = 10
            )),
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE
        ) { field ->
            assertEquals("10", field.value)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_in_yard_distance_to_residence)
            assertTrue(field.settings.readOnly)
        }

        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                inYardDistanceToResidence = null
            )),
            fieldId = CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE
        )
    }

    @Test
    fun testLitter() = runBlockingTest {
        assertFieldExists<StringField<CommonObservationField>>(
            controller = getController(observation = createObservation().copy(
                litter = true
            )),
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_LITTER
        ) { field ->
            field.value.assertEquals(RR.string.generic_yes)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_litter)
            assertTrue(field.settings.readOnly)
        }

        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                litter = false
            )),
            fieldId = CommonObservationField.TASSU_LITTER
        )
    }

    @Test
    fun testPack() = runBlockingTest {
        assertFieldExists<StringField<CommonObservationField>>(
            controller = getController(observation = createObservation().copy(
                pack = true
            )),
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_PACK
        ) { field ->
            field.value.assertEquals(RR.string.generic_yes)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_pack)
            assertTrue(field.settings.readOnly)
        }

        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                pack = false
            )),
            fieldId = CommonObservationField.TASSU_PACK
        )
    }

    @Test
    fun testDeletingObservationMarksItDeleted() = runBlockingTest {
        val observationContext = getObservationContext()
        val (controller, observation) = getControllerAndObservation(observationContext)

        // not loaded yet
        assertNull(observationContext.observationProvider.observations)

        controller.loadViewModel(refresh = false)

        assertEquals(1, observationContext.observationProvider.observations?.size)

        assertTrue(controller.deleteObservation(updateToBackend = false), "deletion")

        observationContext.observationProvider.fetch(refresh = true)

        assertEquals(0, observationContext.observationProvider.observations?.size)

        assertNotNull(observation.localId, "missing observation local id")
        val deletedObservation = observationContext.repository.getByLocalId(observation.localId!!)
        assertTrue(deletedObservation.deleted, "observation deleted")
    }

    @Test
    fun testDeletingObservationCallsBackend() = runBlockingTest {
        val observationContext = getObservationContext()
        val (controller, observation) = getControllerAndObservation(observationContext)

        assertEquals(0, backendAPIMock.callCount(BackendAPI::deleteObservation))

        controller.loadViewModel(refresh = false)
        assertTrue(controller.deleteObservation(updateToBackend = true), "deletion")

        assertEquals(
            expected = 1,
            actual = backendAPIMock.callCount(BackendAPI::deleteObservation),
            message = "delete count"
        )
        assertEquals(
            expected = observation.remoteId,
            actual = backendAPIMock.callParameter(BackendAPI::deleteObservation),
            message = "deleted observation"
        )
    }

    private fun String?.assertEquals(expected: RR.string) {
        assertEquals(stringProvider.getString(expected), this)
    }

    private suspend fun <FieldType> assertFieldExists(
        controller: ViewObservationController,
        expectedIndex: Int,
        fieldId: CommonObservationField,
        fieldAssertions: ((FieldType) -> Unit)? = null
    ) {
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModelOrNull()
        assertNotNull(viewModel)

        val field: FieldType = viewModel.fields.getField(expectedIndex, fieldId)
        fieldAssertions?.let { it(field)}
    }

    private suspend fun assertFieldDoesntExist(
        controller: ViewObservationController,
        fieldId: CommonObservationField,
    ) {
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModelOrNull()
        assertNotNull(viewModel)

        assertFalse(viewModel.fields.map { it.id }.toSet().contains(fieldId))
    }

    private fun createObservation(speciesCode: SpeciesCode = SpeciesCodes.BEAR_ID) =
        CommonObservation(
            localId = null,
            localUrl = null,
            remoteId = 2,
            revision = 1,
            mobileClientRefId = 1,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = Species.Known(speciesCode = speciesCode),
            observationCategory = ObservationCategory.NORMAL.toBackendEnum(),
            observationType = ObservationType.NAKO.toBackendEnum(),
            deerHuntingType = DeerHuntingType.OTHER.toBackendEnum(),
            deerHuntingOtherTypeDescription = "deerHuntingOtherTypeDescription",
            location = OBSERVATION_LOCATION,
            pointOfTime = OBSERVATION_DATE_TIME,
            description = "description",
            images = EntityImages.noImages(),
            specimens = listOf(
                CommonObservationSpecimen(
                    remoteId = 1,
                    revision = 1,
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = ObservationSpecimenState.HEALTHY.toBackendEnum(),
                    marking = ObservationSpecimenMarking.EARMARK.toBackendEnum(),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                )
            ),
            canEdit = true,
            modified = false,
            deleted = false,
            totalSpecimenAmount = 1,
            mooselikeMaleAmount = 2,
            mooselikeFemaleAmount = 3,
            mooselikeFemale1CalfAmount = 4,
            mooselikeFemale2CalfsAmount = 5,
            mooselikeFemale3CalfsAmount = 6,
            mooselikeFemale4CalfsAmount = 7,
            mooselikeCalfAmount = 8,
            mooselikeUnknownSpecimenAmount = 9,
            observerName = "observerName",
            observerPhoneNumber = "observerPhoneNumber",
            officialAdditionalInfo = "officialAdditionalInfo",
            verifiedByCarnivoreAuthority = true,
            inYardDistanceToResidence = null,
            litter = null,
            pack = null,
        )

    private fun createMetadata(
        speciesCode: SpeciesCode = OBSERVATION_SPECIES_CODE,
        dynamicObservationFieldRequirement: Pair<CommonObservationField, ObservationFieldRequirement>? = null,
    ) = ObservationMetadata(
            lastModified = "<not-relevant>",
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            speciesMetadata = mapOf(
                speciesCode to SpeciesObservationMetadata(
                    speciesCode = speciesCode,
                    observationFields = dynamicObservationFieldRequirement?.let { mapOf(it) } ?: emptyMap(),
                    specimenFields = mapOf(),
                    contextSensitiveFieldSets = listOf(),
                    maxLengthOfPawCentimetres = null,
                    minLengthOfPawCentimetres = null,
                    maxWidthOfPawCentimetres = null,
                    minWidthOfPawCentimetres = null,
                )
            ),
        )

    private suspend fun getController(
        observationContext: ObservationContext = getObservationContext(),
        speciesCode: SpeciesCode = OBSERVATION_SPECIES_CODE,
        observation: CommonObservation = createObservation(speciesCode = speciesCode),
        metadata: ObservationMetadata = createMetadata(speciesCode = speciesCode)
    ): ViewObservationController {
        val (viewObservationController, _) = getControllerAndObservation(
            observationContext = observationContext,
            speciesCode = speciesCode,
            observation = observation,
            metadata = metadata,
        )

        return viewObservationController
    }

    private suspend fun getControllerAndObservation(
        observationContext: ObservationContext = getObservationContext(),
        speciesCode: SpeciesCode = OBSERVATION_SPECIES_CODE,
        observation: CommonObservation = createObservation(speciesCode = speciesCode),
        metadata: ObservationMetadata = createMetadata(speciesCode = speciesCode)
    ): Pair<ViewObservationController, CommonObservation> {
        val response = observationContext.saveObservation(observation)
        val resultingObservation = (response as? ObservationOperationResponse.Success)?.observation

        assertNotNull(resultingObservation, "Failed to save observation")
        assertNotNull(resultingObservation.localId, "Missing observation id after save!")

        val currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
        )

        val metadataProvider = MockMetadataProvider(
            observationMetadata = metadata
        )

        val controller = ViewObservationController(
            observationId = resultingObservation.localId!!,
            observationContext = observationContext,
            userContext = currentUserContextProvider.userContext,
            metadataProvider = metadataProvider,
            stringProvider = stringProvider
        )

        return controller to resultingObservation
    }

    private fun getObservationContext(): ObservationContext {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())

        val mockUserContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            mockUserContextProvider.userLoggedIn(MOCK_USER_INFO)
        }

        return ObservationContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPIMock
            },
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider(),
            commonFileProvider = CommonFileProviderMock(),
            database = database,
            currentUserContextProvider = mockUserContextProvider,
        )
    }

    private val stringProvider: StringProvider = TestStringProvider()
    private val backendAPIMock = BackendAPIMock()

    companion object {
        private const val OBSERVATION_SPECIES_CODE = SpeciesCodes.BEAR_ID

        private val OBSERVATION_LOCATION = ETRMSGeoLocation(
            latitude = 6789568,
            longitude = 330224,
            source = BackendEnum.create(GeoLocationSource.MANUAL),
            accuracy = 1.2,
            altitude = null,
            altitudeAccuracy = null,
        )

        private val OBSERVATION_DATE_TIME = LocalDateTime(2022, 5, 1, 18, 0, 0)

        private val MOCK_USER_INFO = UserInfoDTO(
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

}
