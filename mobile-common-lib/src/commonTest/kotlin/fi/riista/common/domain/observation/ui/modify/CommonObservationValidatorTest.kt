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
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.observation.model.toCommonSpecimenData
import fi.riista.common.domain.observation.ui.ObservationFields
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.StringProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonObservationValidatorTest {

    private val metadataProvider = MockMetadataProvider.INSTANCE
    private val observationFields = ObservationFields(metadataProvider)

    @Test
    fun testMooselikeSpecimenAmount() = runBlockingTest {
        val observation = createObservation().copy(
            specimens = null,
            totalSpecimenAmount = null,
            mooselikeMaleAmount = 0,
            mooselikeFemaleAmount = 0,
            mooselikeFemale1CalfAmount = 0,
            mooselikeFemale2CalfsAmount = 0,
            mooselikeFemale3CalfsAmount = 0,
            mooselikeFemale4CalfsAmount = 0,
            mooselikeCalfAmount = 0,
            mooselikeUnknownSpecimenAmount = 0
        )

        with (observation) {
            val validationErrors = validate(this)

            assertEquals(7, validationErrors.size)
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_MALE_AMOUNT), "MALE")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_FEMALE_AMOUNT), "FEMALE")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_FEMALE_1CALF_AMOUNT), "FEMALE_1CALF")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_FEMALE_2CALFS_AMOUNT), "FEMALE_2CALFS")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_FEMALE_3CALFS_AMOUNT), "FEMALE_3CALFS")
            // only for white-tailed deer
            assertFalse(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_FEMALE_4CALFS_AMOUNT), "FEMALE_4CALFS")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_CALF_AMOUNT), "CALF")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT), "UNKNOWN_SPECIMEN")
        }

        with (observation.copy(
            mooselikeMaleAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Male")
        }

        with (observation.copy(
            mooselikeFemaleAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Female")
        }

        with (observation.copy(
            mooselikeFemale1CalfAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Female1Calf")
        }

        with (observation.copy(
            mooselikeFemale2CalfsAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Female2Calfs")
        }

        with (observation.copy(
            mooselikeFemale3CalfsAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Female3Calfs")
        }

        with (observation.copy(
            mooselikeFemale4CalfsAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(7, validationErrors.size, "Female4Calfs Moose")
        }

        with (observation.copy(
            species = Species.Known(speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID),
            observationCategory = ObservationCategory.DEER_HUNTING.toBackendEnum(),
            mooselikeFemale4CalfsAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Female4Calfs White-tailed deer")
        }

        with (observation.copy(
            mooselikeCalfAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "Calf")
        }

        with (observation.copy(
            mooselikeUnknownSpecimenAmount = 1,
        )) {
            val validationErrors = validate(this)
            assertEquals(0, validationErrors.size, "UnknownSpecimen")
        }
    }

    @Test
    fun testMissingSpecimens() = runBlockingTest {
        val observation = createObservation().copy(
            species = Species.Known(speciesCode = SpeciesCodes.BEAN_GOOSE_ID),
            specimens = null,
            observationCategory = BackendEnum.create(ObservationCategory.NORMAL),
        )

        with (observation) {
            val validationErrors = validate(
                observation = this,
            )
            assertEquals(0, validationErrors.size, "hardcoded metadata")
            assertFalse(validationErrors.contains(CommonObservationValidator.Error.MISSING_SPECIMENS),"hardcoded metadata")
        }

        with (observation) {
            val hardcodedMetadata = metadataProvider.observationMetadata

            // use custom metadata as current hard-coded one doesn't require specimens
            val customMetadata = ObservationMetadata(
                lastModified = "right now!", // not used anywhere in validation, hopefully!
                speciesMetadata = mapOf(
                    SpeciesCodes.BEAN_GOOSE_ID to hardcodedMetadata.speciesMetadata[SpeciesCodes.BEAN_GOOSE_ID]!!.copy(
                        specimenFields = mapOf(
                            ObservationSpecimenField.AGE to ObservationFieldRequirement.YES
                        ),
                        contextSensitiveFieldSets = listOf() // clear these so they won't override specimenFields
                    )
                ),
                observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            )
            val validationErrors = validate(
                observation = this,
                observationMetadata = customMetadata
            )
            assertEquals(1, validationErrors.size, "custom metadata")
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.MISSING_SPECIMENS),"custom metadata")
        }
    }

    @Test
    fun testMinimumSpecimenAmountForPoikueIsTwo() = runBlockingTest {
        var observation = createObservation().copy(
            species = Species.Known(speciesCode = SpeciesCodes.BEAN_GOOSE_ID),
            specimens = listOf(
                CommonObservationSpecimen(
                    remoteId = 1,
                    revision = 1,
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = null.toBackendEnum(),
                    marking = null.toBackendEnum(),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                )
            ).map { it.toCommonSpecimenData() },
            observationCategory = BackendEnum.create(ObservationCategory.NORMAL),
            observationType = BackendEnum.create(ObservationType.POIKUE),
            totalSpecimenAmount = 1,
            mooselikeMaleAmount = null,
            mooselikeFemaleAmount = null,
            mooselikeFemale1CalfAmount = null,
            mooselikeFemale2CalfsAmount = null,
            mooselikeFemale3CalfsAmount = null,
            mooselikeFemale4CalfsAmount = null,
            mooselikeCalfAmount = null,
            mooselikeUnknownSpecimenAmount = null,
        )

        with (observation) {
            val validationErrors = validate(
                observation = this,
            )
            assertEquals(1, validationErrors.size)
            assertTrue(validationErrors.contains(CommonObservationValidator.Error.SPECIMEN_AMOUNT_AT_LEAST_TWO))
        }

        observation = observation.copy(
            specimens = listOf(
                CommonObservationSpecimen(
                    remoteId = 1,
                    revision = 1,
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = null.toBackendEnum(),
                    marking = null.toBackendEnum(),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                ),
                CommonObservationSpecimen(
                    remoteId = 1,
                    revision = 1,
                    gender = Gender.FEMALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = null.toBackendEnum(),
                    marking = null.toBackendEnum(),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                ),
            ).map { it.toCommonSpecimenData() },
            totalSpecimenAmount = 2,
        )
        with (observation) {
            val validationErrors = validate(
                observation = this,
            )
            assertEquals(0, validationErrors.size)
        }
    }

    private fun validate(
        observation: CommonObservationData,
        mode: ObservationFields.Context.Mode = ObservationFields.Context.Mode.EDIT,
        observationMetadata: ObservationMetadata = metadataProvider.observationMetadata
    ): List<CommonObservationValidator.Error> {
        return CommonObservationValidator.validate(
            observation = observation,
            observationMetadata = observationMetadata,
            displayedFields = observationFields.getFieldsToBeDisplayed(
                context = ObservationFields.Context(
                    observation = observation,
                    userIsCarnivoreAuthority = false,
                    mode = mode,
                )
            )
        )
    }

    private fun createObservation(speciesCode: SpeciesCode = OBSERVATION_SPECIES_CODE) =
        CommonObservationData(
            localId = 1,
            localUrl = null,
            remoteId = 2,
            revision = 1,
            mobileClientRefId = 1,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = Species.Known(speciesCode = speciesCode),
            observationCategory = ObservationCategory.MOOSE_HUNTING.toBackendEnum(),
            observationType = ObservationType.NAKO.toBackendEnum(),
            deerHuntingType = DeerHuntingType.OTHER.toBackendEnum(),
            deerHuntingOtherTypeDescription = "deerHuntingOtherTypeDescription",
            location = OBSERVATION_LOCATION.asKnownLocation(),
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
            ).map { it.toCommonSpecimenData() },
            canEdit = true,
            modified = true,
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

    val stringProvider: StringProvider = TestStringProvider()

    companion object {
        private const val OBSERVATION_SPECIES_CODE = SpeciesCodes.MOOSE_ID

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
