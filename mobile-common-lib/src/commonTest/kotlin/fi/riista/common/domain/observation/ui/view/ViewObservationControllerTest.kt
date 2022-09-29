package fi.riista.common.domain.observation.ui.view

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.*
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
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.getField
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.model.*
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.*
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

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
    fun testMapIsAlwaysPresent() {
        assertFieldExists<LocationField<CommonObservationField>>(
            expectedIndex = 0,
            fieldId = CommonObservationField.LOCATION
        ) { field ->
            assertEquals(OBSERVATION_LOCATION.asKnownLocation(), field.location)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCodeIsAlwaysPresent() {
        assertFieldExists<SpeciesField<CommonObservationField>>(
            expectedIndex = 1,
            fieldId = CommonObservationField.SPECIES_AND_IMAGE
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.species)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testDateTimeIsAlwaysPresent() {
        assertFieldExists<DateAndTimeField<CommonObservationField>>(
            expectedIndex = 2,
            fieldId = CommonObservationField.DATE_AND_TIME
        ) { field ->
            assertEquals(OBSERVATION_DATE_TIME, field.dateAndTime)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testObservationTypeIsAlwaysPresent() {
        assertFieldExists<StringField<CommonObservationField>>(
            expectedIndex = 3,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        ) { field ->
            field.value.assertEquals(ObservationType.NAKO.resourcesStringId)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_observation_type)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testDescriptionIsAlwaysPresent() {
        assertFieldExists<StringField<CommonObservationField>>(
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
    fun testObservationCategory() {
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
    fun testWithinMooseHunting() {
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
    fun testWithinDeerHunting() {
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
    fun testDeerHuntingType() {
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
            field.settings.label.assertEquals(RR.string.group_hunting_harvest_field_deer_hunting_type)
            assertTrue(field.settings.readOnly)
        }

        assertFieldDoesntExist(controller, CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION)
    }

    @Test
    fun testDeerHuntingOtherTypeDescription() {
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
            field.settings.label.assertEquals(RR.string.group_hunting_harvest_field_deer_hunting_other_type_description)
            assertTrue(field.settings.readOnly)
        }
    }

    @Test
    fun testAmountCausesSpecimensToBeDisplayed() {
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
    fun testMooselikeMaleAmount() {
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
    fun testMooselikeFemaleAmount() {
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
    fun testMooselikeFemale1CalfAmount() {
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
    fun testMooselikeFemale2CalfsAmount() {
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
    fun testMooselikeFemale3CalfsAmount() {
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
    fun testMooselikeFemale4CalfsAmount() {
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
    fun testMooselikeCalfAmount() {
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
    fun testMooselikeUnknownSpecimenAmount() {
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
    fun testVerifiedByCarnivoreAuthority() {
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
    fun testObserverName() {
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
    fun testObserverPhoneNumber() {
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
    fun testOfficialAdditionalInfo() {
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
    fun testInYardDistanceToResidence() {
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
    fun testLitter() {
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
    fun testPack() {
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

    private fun String?.assertEquals(expected: RR.string) {
        assertEquals(stringProvider.getString(expected), this)
    }

    private fun <FieldType> assertFieldExists(
        controller: ViewObservationController = getController(),
        expectedIndex: Int,
        fieldId: CommonObservationField,
        fieldAssertions: ((FieldType) -> Unit)? = null
    ) = runBlockingTest {
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModelOrNull()
        assertNotNull(viewModel)

        val field: FieldType = viewModel.fields.getField(expectedIndex, fieldId)
        fieldAssertions?.let { it(field)}
    }

    private fun assertFieldDoesntExist(
        controller: ViewObservationController = getController(),
        fieldId: CommonObservationField,
    ) = runBlockingTest {
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModelOrNull()
        assertNotNull(viewModel)

        assertFalse(viewModel.fields.map { it.id }.toSet().contains(fieldId))
    }

    private fun createObservation(speciesCode: SpeciesCode = SpeciesCodes.BEAR_ID) =
        CommonObservation(
            localId = 1,
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

    private fun getController(
        speciesCode: SpeciesCode = OBSERVATION_SPECIES_CODE,
        observation: CommonObservation = createObservation(speciesCode = speciesCode),
        metadata: ObservationMetadata = createMetadata(speciesCode = speciesCode)
    ): ViewObservationController {
        val currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider()
        )

        val metadataProvider = MockMetadataProvider(
            observationMetadata = metadata
        )

        val controller = ViewObservationController(
            userContext = currentUserContextProvider.userContext,
            metadataProvider = metadataProvider,
            stringProvider = stringProvider
        )
        controller.observation = observation

        return controller
    }

    val stringProvider: StringProvider = TestStringProvider()

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
    }

}