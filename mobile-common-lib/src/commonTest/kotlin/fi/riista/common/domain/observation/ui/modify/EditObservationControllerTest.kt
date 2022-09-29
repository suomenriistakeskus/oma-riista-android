package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
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
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.TrueOrFalse
import fi.riista.common.model.toBackendEnum
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class EditObservationControllerTest {

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
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCodeIsAlwaysPresent() {
        assertFieldExists<SpeciesField<CommonObservationField>>(
            expectedIndex = 1,
            fieldId = CommonObservationField.SPECIES_AND_IMAGE
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.species)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testDateTimeIsAlwaysPresent() {
        assertFieldExists<DateAndTimeField<CommonObservationField>>(
            expectedIndex = 2,
            fieldId = CommonObservationField.DATE_AND_TIME
        ) { field ->
            assertEquals(OBSERVATION_DATE_TIME, field.dateAndTime)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testObservationTypeIsAlwaysPresent() {
        assertFieldExists<StringListField<CommonObservationField>>(
            expectedIndex = 3,
            fieldId = CommonObservationField.OBSERVATION_TYPE
        ) { field ->
            assertEquals(listOf(ObservationType.NAKO.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_observation_type)
            assertFalse(field.settings.readOnly)
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
            assertFalse(field.settings.readOnly)
        }
    }


    // fields depending on metadata

    @Test
    fun testObservationCategory() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.OBSERVATION_CATEGORY to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<StringListField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.OBSERVATION_CATEGORY
        ) { field ->
            assertEquals(listOf(ObservationCategory.NORMAL.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.observation_label_observation_category)
            assertFalse(field.settings.readOnly)
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
        assertFieldExists<StringListField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.WITHIN_MOOSE_HUNTING
        ) { field ->
            assertEquals(listOf(TrueOrFalse.FALSE.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.observation_label_within_moose_hunting)
            assertFalse(field.settings.readOnly)
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
        assertFieldExists<StringListField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 3, // preceeds observation type
            fieldId = CommonObservationField.WITHIN_DEER_HUNTING
        ) { field ->
            assertEquals(listOf(TrueOrFalse.FALSE.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.observation_label_within_deer_hunting)
            assertFalse(field.settings.readOnly)
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
        assertFieldExists<StringListField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.DEER_HUNTING_TYPE
        ) { field ->
            assertEquals(listOf(DeerHuntingType.OTHER.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.group_hunting_harvest_field_deer_hunting_type)
            assertFalse(field.settings.readOnly)
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
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testAmountCausesSpecimensToBeDisplayed() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement = CommonObservationField.SPECIMEN_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))
        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.SPECIMEN_AMOUNT
        ) { field ->
            assertEquals(1, field.value)
            field.settings.label.assertEquals(RR.string.observation_label_amount)
            assertFalse(field.settings.readOnly)
        }

        assertFieldExists<SpecimenField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 5,
            fieldId = CommonObservationField.SPECIMENS
        ) { field ->
            assertEquals(Species.Known(OBSERVATION_SPECIES_CODE), field.specimenData.species)
            assertEquals(1, field.specimenData.specimens.size)
        }

        assertFieldExists<StringField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 6,
            fieldId = CommonObservationField.DESCRIPTION
        )
    }

    @Test
    fun testMooselikeMaleAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_MALE_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_MALE_AMOUNT
        ) { field ->
            assertEquals(2, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_male_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemaleAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT
        ) { field ->
            assertEquals(3, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale1CalfAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT
        ) { field ->
            assertEquals(4, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_1calf_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale2CalfsAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT
        ) { field ->
            assertEquals(5, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_2calf_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale3CalfsAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT
        ) { field ->
            assertEquals(6, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_3calf_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeFemale4CalfsAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT
        ) { field ->
            assertEquals(7, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_female_4calf_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeCalfAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_CALF_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_CALF_AMOUNT
        ) { field ->
            assertEquals(8, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_calf_amount)
            assertFalse(field.settings.readOnly)
        }
    }

    @Test
    fun testMooselikeUnknownSpecimenAmount() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<IntField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT
        ) { field ->
            assertEquals(9, field.value)
            field.settings.label.assertEquals(RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount)
            assertFalse(field.settings.readOnly)
        }
    }



    // Tassu fields

    @Test
    fun testVerifiedByCarnivoreAuthority() {
        val controller = getController(metadata = createMetadata(
            dynamicObservationFieldRequirement =
            CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY to ObservationFieldRequirement.VOLUNTARY,
        ))

        assertFieldExists<StringListField<CommonObservationField>>(
            controller = controller,
            expectedIndex = 4,
            fieldId = CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY
        ) { field ->
            assertEquals(listOf(TrueOrFalse.TRUE.ordinal.toLong()), field.selected)
            field.settings.label.assertEquals(RR.string.observation_label_tassu_verified_by_carnivore_authority)
            assertFalse(field.settings.readOnly)
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
            assertFalse(field.settings.readOnly)
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
            assertFalse(field.settings.readOnly)
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
            assertFalse(field.settings.readOnly)
        }
    }


    // Tassu fields, readonly, depending on current observation values

    @Test
    fun testInYardDistanceToResidence() {
        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                inYardDistanceToResidence = 10
            )),
            fieldId = CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE
        )

        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                inYardDistanceToResidence = null
            )),
            fieldId = CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE
        )
    }

    @Test
    fun testLitter() {
        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                litter = true
            )),
            fieldId = CommonObservationField.TASSU_LITTER
        )

        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                litter = false
            )),
            fieldId = CommonObservationField.TASSU_LITTER
        )
    }

    @Test
    fun testPack() {
        assertFieldDoesntExist(
            controller = getController(observation = createObservation().copy(
                pack = true
            )),
            fieldId = CommonObservationField.TASSU_PACK
        )

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
        controller: EditObservationController = getController(),
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
        controller: EditObservationController = getController(),
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
    ): EditObservationController {
        val currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider()
        )

        val metadataProvider = MockMetadataProvider(
            observationMetadata = metadata
        )

        val controller = EditObservationController(
            userContext = currentUserContextProvider.userContext,
            metadataProvider = metadataProvider,
            stringProvider = stringProvider
        )
        controller.editableObservation = EditableObservation(observation = observation)

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