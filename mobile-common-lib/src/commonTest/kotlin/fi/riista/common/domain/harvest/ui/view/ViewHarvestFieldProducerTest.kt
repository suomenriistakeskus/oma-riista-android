package fi.riista.common.domain.harvest.ui.view

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.*
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.helpers.TestHarvestPermitProvider
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.model.*
import fi.riista.common.resources.MockLanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.ui.dataField.*
import kotlin.test.*

class ViewHarvestFieldProducerTest {

    private val fieldProducer = ViewHarvestFieldProducer(
        harvestPermitProvider = TestHarvestPermitProvider.INSTANCE,
        stringProvider = TestStringProvider.INSTANCE,
        languageProvider = MockLanguageProvider(),
    )

    @Test
    fun testLocation() {
        createHarvest().assertCreatedField<LocationField<CommonHarvestField>>(CommonHarvestField.LOCATION) {
            assertEquals(HARVEST_LOCATION.asKnownLocation(), location)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCodeAndImage() {
        createHarvest().copy(
            images = EntityImages(remoteImageIds = listOf("1234"), localImages = listOf())
        ).assertCreatedField<SpeciesField<CommonHarvestField>>(
            CommonHarvestField.SPECIES_CODE_AND_IMAGE
        ) {
            assertEquals("1234", entityImage?.serverId)
            assertTrue(settings.showEntityImage)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCode() {
        createHarvest().assertCreatedField<SpeciesField<CommonHarvestField>>(
            CommonHarvestField.SPECIES_CODE
        ) {
            assertEquals(null, entityImage)
            assertFalse(settings.showEntityImage)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testDateAndTime() {
        createHarvest().assertCreatedField<DateAndTimeField<CommonHarvestField>>(
            CommonHarvestField.DATE_AND_TIME
        ) {
            assertEquals(HARVEST_DATE_TIME, dateAndTime)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testDeerHuntingType() {
        createHarvest().copy(
            deerHuntingType = DeerHuntingType.DOG_HUNTING.toBackendEnum()
        ).assertStringField(
            CommonHarvestField.DEER_HUNTING_TYPE
        ) {
            assertEquals("dog_hunting", value)
            assertEquals("deer_hunting_type", settings.label)
        }
    }

    @Test
    fun testDeerHuntingOtherTypeDescription() {
        createHarvest().copy(
            deerHuntingOtherTypeDescription = "deerHuntingOtherTypeDescription"
        ).assertStringField(
            CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION
        ) {
            assertEquals("deerHuntingOtherTypeDescription", value)
            assertEquals("deer_hunting_other_type_description", settings.label)
        }
    }

    @Test
    fun testActor() {
        createHarvest().copy(
            actorInfo = GroupHuntingPerson.Guest(
                PersonWithHunterNumber(
                    id = 1,
                    rev = 1,
                    byName = "first",
                    lastName = "last",
                    hunterNumber = "88888888"
                )
            )
        ).assertStringField(
            CommonHarvestField.ACTOR
        ) {
            assertEquals("first last", value)
            assertEquals("actor", settings.label)
        }
    }

    @Test
    fun testAuthor() {
        createHarvest().copy(
            authorInfo = PersonWithHunterNumber(
                id = 1,
                rev = 1,
                byName = "first",
                lastName = "last",
                hunterNumber = "88888888"
            )
        ).assertStringField(
            CommonHarvestField.AUTHOR
        ) {
            assertEquals("first last", value)
            assertEquals("author", settings.label)
        }
    }

    @Test
    fun testGender() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    gender = Gender.MALE.toBackendEnum()
                )
            )
        ).assertCreatedField<GenderField<CommonHarvestField>>(
            CommonHarvestField.GENDER
        ) {
            assertEquals(Gender.MALE, gender)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testAge() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    age = GameAge.ADULT.toBackendEnum()
                )
            )
        ).assertCreatedField<AgeField<CommonHarvestField>>(
            CommonHarvestField.AGE
        ) {
            assertEquals(GameAge.ADULT, age)
            assertTrue(settings.readOnly)
        }
    }

    @Test
    fun testAmount() {
        createHarvest().assertStringField(
            CommonHarvestField.SPECIMEN_AMOUNT
        ) {
            assertEquals("1", value)
            assertEquals("amount", settings.label)
        }
    }

    @Test
    fun testSpecimens() {
        val harvest = createHarvest(
            speciesCode = SpeciesCodes.GREYLAG_GOOSE_ID
        ).copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    age = GameAge.ADULT.toBackendEnum(),
                    gender = Gender.FEMALE.toBackendEnum(),
                    weight = 35.0
                )
            )
        )

        harvest.assertCreatedField<SpecimenField<CommonHarvestField>>(
            CommonHarvestField.SPECIMENS
        ) {
            assertTrue(settings.readOnly)

            with (specimenData) {
                assertEquals(harvest.species, species)
                assertEquals(harvest.specimens, specimens)
                assertEquals(
                    expected = listOf(
                        SpecimenFieldSpecification(
                            fieldType = SpecimenFieldType.GENDER,
                            label = TestStringProvider.INSTANCE.getString(RR.string.gender_label),
                            requirementStatus = FieldRequirement.noRequirement(),
                        ),
                        SpecimenFieldSpecification(
                            fieldType = SpecimenFieldType.AGE,
                            label = TestStringProvider.INSTANCE.getString(RR.string.age_label),
                            requirementStatus = FieldRequirement.noRequirement(),
                        ),
                        SpecimenFieldSpecification(
                            fieldType = SpecimenFieldType.WEIGHT,
                            label = TestStringProvider.INSTANCE.getString(RR.string.harvest_label_weight),
                            requirementStatus = FieldRequirement.noRequirement(),
                        )
                    ),
                    actual = fieldSpecifications,
                )
                assertEquals(
                    expected = listOf(GameAge.ADULT, GameAge.YOUNG).map { BackendEnum.create(it) },
                    actual = allowedAges
                )
            }
        }
    }

    @Test
    fun testNotEdible() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    notEdible = true
                )
            )
        ).assertStringField(
            CommonHarvestField.NOT_EDIBLE
        ) {
            assertEquals("yes", value)
            assertEquals("not_edible", settings.label)
        }
    }

    @Test
    fun testWeight() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weight = 23.0
                )
            )
        ).assertStringField(
            CommonHarvestField.WEIGHT
        ) {
            assertEquals("23", value)
            assertEquals("weight", settings.label)
        }
    }

    @Test
    fun testWeightEstimated() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weightEstimated = 24.0
                )
            )
        ).assertStringField(
            CommonHarvestField.WEIGHT_ESTIMATED
        ) {
            assertEquals("24", value)
            assertEquals("weight_estimated", settings.label)
        }
    }

    @Test
    fun testWeightMeasured() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weightMeasured = 25.0
                )
            )
        ).assertStringField(
            CommonHarvestField.WEIGHT_MEASURED
        ) {
            assertEquals("25", value)
            assertEquals("weight_measured", settings.label)
        }
    }

    @Test
    fun testFitnessClass() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    fitnessClass = GameFitnessClass.EXCELLENT.toBackendEnum()
                )
            )
        ).assertStringField(
            CommonHarvestField.FITNESS_CLASS
        ) {
            assertEquals("fitness_class_erinomainen", value)
            assertEquals("fitness_class", settings.label)
        }
    }

    @Test
    fun testAntlersType() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersType = GameAntlersType.CERVINE.toBackendEnum()
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_TYPE
        ) {
            assertEquals("antler_type_hanko", value)
            assertEquals("antlers_type", settings.label)
        }
    }

    @Test
    fun testAntlersWidth() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersWidth = 37
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_WIDTH
        ) {
            assertEquals("37", value)
            assertEquals("antlers_width", settings.label)
        }
    }

    @Test
    fun testAntlersPointsLeft() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlerPointsLeft = 12
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLER_POINTS_LEFT
        ) {
            assertEquals("12", value)
            assertEquals("antler_points_left", settings.label)
        }
    }

    @Test
    fun testAntlersPointsRight() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlerPointsRight = 15
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLER_POINTS_RIGHT
        ) {
            assertEquals("15", value)
            assertEquals("antler_points_right", settings.label)
        }
    }

    @Test
    fun testAntlersLost() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersLost = true
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_LOST
        ) {
            assertEquals("yes", value)
            assertEquals("antlers_lost", settings.label)
        }
    }

    @Test
    fun testAntlersGirth() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersGirth = 15
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_GIRTH
        ) {
            assertEquals("15", value)
            assertEquals("antlers_girth", settings.label)
        }
    }

    @Test
    fun testAntlerShaftWidth() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlerShaftWidth = 12
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLER_SHAFT_WIDTH
        ) {
            assertEquals("12", value)
            assertEquals("antler_shaft_width", settings.label)
        }
    }

    @Test
    fun testAntlersLength() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersLength = 10
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_LENGTH
        ) {
            assertEquals("10", value)
            assertEquals("antlers_length", settings.label)
        }
    }

    @Test
    fun testAntlerInnerWidth() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    antlersInnerWidth = 10
                )
            )
        ).assertStringField(
            CommonHarvestField.ANTLERS_INNER_WIDTH
        ) {
            assertEquals("10", value)
            assertEquals("antlers_inner_width", settings.label)
        }
    }

    @Test
    fun testAlone() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    alone = true
                )
            )
        ).assertStringField(
            CommonHarvestField.ALONE
        ) {
            assertEquals("yes", value)
            assertEquals("alone", settings.label)
        }
    }

    @Test
    fun testAdditionalInformation() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    additionalInfo = "info"
                )
            )
        ).assertStringField(
            CommonHarvestField.ADDITIONAL_INFORMATION
        ) {
            assertEquals("info", value)
            assertEquals("additional_information", settings.label)
        }
    }

    @Test
    fun testPermitNumber() {
        // ensures that fallback permit type is used
        TestHarvestPermitProvider.INSTANCE.mockPermit = TestHarvestPermitProvider.INSTANCE.mockPermit.copy(permitNumber = "permitNumber")
        assertNotEquals("123", TestHarvestPermitProvider.INSTANCE.mockPermit.permitNumber)

        createHarvest().copy(
            permitType = "type",
            permitNumber = "123"
        ).assertStringField(
            CommonHarvestField.PERMIT_INFORMATION
        ) {
            assertEquals("type\n123", value)
            assertEquals("permit_information", settings.label)
        }
    }

    @Test
    fun testPermitTypeFromPermitIsUsed() {
        // ensures that there's a permit for permitNumber
        TestHarvestPermitProvider.INSTANCE.mockPermit = TestHarvestPermitProvider.INSTANCE.mockPermit.copy(
            permitNumber = "123",
        )
        assertEquals("123", TestHarvestPermitProvider.INSTANCE.mockPermit.permitNumber)

        createHarvest().copy(
            permitType = "type",
            permitNumber = "123"
        ).assertStringField(
            CommonHarvestField.PERMIT_INFORMATION
        ) {
            assertEquals("mockPermit\n123", value)
            assertEquals("permit_information", settings.label)
        }
    }

    @Test
    fun testHarvestReportState() {
        fieldProducer.createField(
            fieldSpecification = CommonHarvestField.HARVEST_REPORT_STATE.noRequirement(),
            harvest = createHarvest()
        ).let {
            assertNull(it)
        }

        createHarvest().copy(
            harvestReportRequired = true
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_report_required,
            expectedColor = IndicatorColor.RED
        )

        createHarvest().copy(
            harvestReportState = HarvestReportState.SENT_FOR_APPROVAL.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_report_state_sent_for_approval,
            expectedColor = IndicatorColor.YELLOW
        )

        createHarvest().copy(
            harvestReportState = HarvestReportState.REJECTED.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_report_state_rejected,
            expectedColor = IndicatorColor.RED
        )

        createHarvest().copy(
            harvestReportState = HarvestReportState.APPROVED.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_report_state_approved,
            expectedColor = IndicatorColor.GREEN
        )

        createHarvest().copy(
            stateAcceptedToHarvestPermit = StateAcceptedToHarvestPermit.PROPOSED.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_permit_proposed,
            expectedColor = IndicatorColor.YELLOW
        )

        createHarvest().copy(
            stateAcceptedToHarvestPermit = StateAcceptedToHarvestPermit.REJECTED.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_permit_rejected,
            expectedColor = IndicatorColor.RED
        )

        createHarvest().copy(
            stateAcceptedToHarvestPermit = StateAcceptedToHarvestPermit.ACCEPTED.toBackendEnum()
        ).assertHarvestReportField(
            expectedText = RR.string.harvest_permit_accepted,
            expectedColor = IndicatorColor.GREEN
        )
    }

    private inline fun CommonHarvestData.assertHarvestReportField(
        expectedText: RR.string,
        expectedColor: IndicatorColor,
    ) {
        assertHarvestReportField(
            expectedText = TestStringProvider.INSTANCE.getString(expectedText),
            expectedColor = expectedColor
        )
    }

    private fun CommonHarvestData.assertHarvestReportField(
        expectedText: String,
        expectedColor: IndicatorColor,
    ) {
        assertCreatedField<LabelField<CommonHarvestField>>(
            CommonHarvestField.HARVEST_REPORT_STATE
        ) {
            assertEquals(expectedText, text)
            assertEquals(expectedColor, settings.indicatorColor, expectedText)
        }
    }

    @Test
    fun testFeedingPlace() {
        createHarvest().copy(
            feedingPlace = true,
        ).assertStringField(
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE
        ) {
            assertEquals("yes", value)
            assertEquals("feeding_place", settings.label)
        }
    }

    @Test
    fun testHuntingMethod() {
        createHarvest().copy(
            greySealHuntingMethod = GreySealHuntingMethod.CAPTURED_ALIVE.toBackendEnum(),
        ).assertStringField(
            CommonHarvestField.GREY_SEAL_HUNTING_METHOD
        ) {
            assertEquals("grey_seal_hunting_method_captured_alive", value)
            assertEquals("grey_seal_hunting_method", settings.label)
        }
    }

    @Test
    fun testTaigaBeanGoose() {
        createHarvest().copy(
            taigaBeanGoose = false,
        ).assertStringField(
            CommonHarvestField.IS_TAIGA_BEAN_GOOSE
        ) {
            assertEquals("no", value)
            assertEquals("is_taiga_bean_goose", settings.label)
        }
    }

    @Test
    fun testNotSupportedFields() {
        val harvest = createHarvest()

        listOf(
            CommonHarvestField.ACTOR_HUNTER_NUMBER,
            CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
            CommonHarvestField.ANTLER_INSTRUCTIONS,
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
            CommonHarvestField.HUNTING_DAY_AND_TIME,
            CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT,
            CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
            CommonHarvestField.HEADLINE_SHOOTER,
            CommonHarvestField.HEADLINE_SPECIMEN,
            CommonHarvestField.PERMIT_REQUIRED_NOTIFICATION
        ).forEach { field ->
            val producedField = fieldProducer.createField(
                fieldSpecification = field.noRequirement(),
                harvest = harvest
            )

            assertNull(producedField, "field $field")
        }
    }

    private fun CommonHarvestData.assertStringField(
        field: CommonHarvestField,
        fieldAssertions: StringField<CommonHarvestField>.() -> Unit
    ) {
        assertCreatedField<StringField<CommonHarvestField>>(field) {
            assertTrue(settings.singleLine, "field $field")
            assertTrue(settings.readOnly, "field $field")

            fieldAssertions()
        }
    }

    private fun <FieldType: DataField<CommonHarvestField>> CommonHarvestData.assertCreatedField(
        field: CommonHarvestField,
        fieldAssertions: FieldType.() -> Unit
    ) {
        assertCreatedField(
            fieldSpecification = field.noRequirement(),
            fieldAssertions = fieldAssertions
        )
    }

    private fun <FieldType: DataField<CommonHarvestField>> CommonHarvestData.assertCreatedField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        fieldAssertions: FieldType.() -> Unit
    ) {
        val producedField = fieldProducer.createField(
            fieldSpecification = fieldSpecification,
            harvest = this
        )

        assertNotNull(producedField, "Field: $fieldSpecification")

        @Suppress("UNCHECKED_CAST")
        val typedField = try {
            producedField as FieldType
        } catch (e: Throwable) {
            fail("Field $id type was ${producedField::class} instead of expected one")
        }

        typedField.fieldAssertions()
    }

    private fun createHarvest(speciesCode: SpeciesCode = HARVEST_SPECIES_CODE) =
        CommonHarvestData(
            localId = null,
            localUrl = null,
            id = 1,
            rev = 1,
            species = Species.Known(speciesCode),
            location = HARVEST_LOCATION.asKnownLocation(),
            pointOfTime = HARVEST_DATE_TIME,
            description = "description",
            canEdit = false,
            modified = false,
            deleted = false,
            images = EntityImages.noImages(),
            specimens = listOf(
                CommonSpecimenData.createForTests()
            ),
            amount = 1,
            huntingDayId = null,
            authorInfo = null,
            actorInfo = GroupHuntingPerson.Unknown,
            selectedClub = SearchableOrganization.Unknown,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = false,
            harvestReportState = BackendEnum.create(null),
            permitNumber = null,
            permitType = null,
            stateAcceptedToHarvestPermit = BackendEnum.create(null),
            deerHuntingType = BackendEnum.create(null),
            deerHuntingOtherTypeDescription = null,
            mobileClientRefId = 1,
            harvestReportDone = false,
            rejected = false,
            feedingPlace = null,
            taigaBeanGoose = null,
            greySealHuntingMethod = BackendEnum.create(null),
        )

    companion object {
        private const val HARVEST_SPECIES_CODE = SpeciesCodes.MOOSE_ID

        private val HARVEST_LOCATION = ETRMSGeoLocation(
            latitude = 6789568,
            longitude = 330224,
            source = BackendEnum.create(GeoLocationSource.MANUAL),
            accuracy = 1.2,
            altitude = null,
            altitudeAccuracy = null,
        )

        private val HARVEST_DATE_TIME = LocalDateTime(2022, 5, 1, 18, 0, 0)
    }

}
