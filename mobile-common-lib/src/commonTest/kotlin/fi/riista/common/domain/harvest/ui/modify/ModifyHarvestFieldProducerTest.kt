package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.fields.HarvestSpecimenFieldRequirementResolver
import fi.riista.common.domain.huntingclub.selectableForEntries.MockHuntingClubsSelectableForEntries
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.GameAntlersType
import fi.riista.common.domain.model.GameFitnessClass
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.createForTests
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.userInfo.MockUsernameProvider
import fi.riista.common.helpers.TestHarvestPermitProvider
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.StringWithId
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.MockLanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ModifyHarvestFieldProducerTest {

    private val fieldProducer = ModifyHarvestFieldProducer(
        canChangeSpecies = true,
        harvestPermitProvider = TestHarvestPermitProvider.INSTANCE,
        huntingClubsSelectableForHarvests = MockHuntingClubsSelectableForEntries(),
        stringProvider = TestStringProvider.INSTANCE,
        languageProvider = MockLanguageProvider(),
        currentDateTimeProvider = MockDateTimeProvider(
            now = HARVEST_DATE_TIME
        )
    )

    @Test
    fun testLocation() {
        createHarvest().assertCreatedField<LocationField<CommonHarvestField>>(CommonHarvestField.LOCATION) {
            assertEquals(HARVEST_LOCATION.asKnownLocation(), location)
            assertFalse(settings.readOnly)
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
            assertFalse(settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCode() {
        createHarvest().assertCreatedField<SpeciesField<CommonHarvestField>>(
            CommonHarvestField.SPECIES_CODE
        ) {
            assertEquals(null, entityImage)
            assertFalse(settings.showEntityImage)
            assertFalse(settings.readOnly)
        }
    }

    @Test
    fun testDateAndTime() {
        createHarvest().assertCreatedField<DateAndTimeField<CommonHarvestField>>(
            CommonHarvestField.DATE_AND_TIME
        ) {
            assertEquals(HARVEST_DATE_TIME, dateAndTime)
            assertEquals(HARVEST_DATE_TIME, settings.maxDateTime)
            assertFalse(settings.readOnly)
        }
    }

    @Test
    fun testDeerHuntingType() {
        createHarvest().copy(
            deerHuntingType = DeerHuntingType.DOG_HUNTING.toBackendEnum()
        ).assertCreatedField<StringListField<CommonHarvestField>>(
            CommonHarvestField.DEER_HUNTING_TYPE
        ) {
            assertEquals(listOf(DeerHuntingType.DOG_HUNTING.ordinal.toLong()), selected)
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
            assertFalse(settings.readOnly)
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
            assertFalse(settings.readOnly)
        }
    }

    @Test
    fun testAmount() {
        createHarvest().assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.SPECIMEN_AMOUNT
        ) {
            assertEquals(1, value)
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

        HarvestReportingType.values().forEach { harvestReportingType ->
            harvest.assertCreatedField<SpecimenField<CommonHarvestField>>(
                field = CommonHarvestField.SPECIMENS,
                harvestReportingType = harvestReportingType,
            ) {
                assertFalse(settings.readOnly)

                with (specimenData) {
                    assertEquals(harvest.species, species)
                    assertEquals(harvest.specimens, specimens)

                    assertEquals(
                        expected = listOf(
                            SpecimenFieldSpecification(
                                fieldType = SpecimenFieldType.GENDER,
                                label = TestStringProvider.INSTANCE.getString(RR.string.gender_label),
                                requirementStatus = FieldRequirement(
                                    type = HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                                        specimenField = CommonHarvestField.GENDER,
                                        species = species,
                                        harvestReportingType = harvestReportingType
                                    ),
                                    indicateRequirement = true,
                                ),
                            ),
                            SpecimenFieldSpecification(
                                fieldType = SpecimenFieldType.AGE,
                                label = TestStringProvider.INSTANCE.getString(RR.string.age_label),
                                requirementStatus = FieldRequirement(
                                    type = HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                                        specimenField = CommonHarvestField.AGE,
                                        species = species,
                                        harvestReportingType = harvestReportingType
                                    ),
                                    indicateRequirement = true,
                                ),
                            ),
                            SpecimenFieldSpecification(
                                fieldType = SpecimenFieldType.WEIGHT,
                                label = TestStringProvider.INSTANCE.getString(RR.string.harvest_label_weight),
                                requirementStatus = FieldRequirement(
                                    type = HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                                        specimenField = CommonHarvestField.WEIGHT,
                                        species = species,
                                        harvestReportingType = harvestReportingType
                                    ),
                                    indicateRequirement = true,
                                ),
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
    }

    @Test
    fun testNotEdible() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    notEdible = true
                )
            )
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.NOT_EDIBLE
        ) {
            assertEquals(true, value)
            assertEquals("not_edible", settings.label)
        }
    }

    @Test
    fun testWeight() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weight = 2.3
                )
            )
        ).assertCreatedField<DoubleField<CommonHarvestField>>(
            CommonHarvestField.WEIGHT
        ) {
            assertEquals(2.3, value)
            assertEquals("weight", settings.label)
        }
    }

    @Test
    fun testWeightEstimated() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weightEstimated = 2.4
                )
            )
        ).assertCreatedField<DoubleField<CommonHarvestField>>(
            CommonHarvestField.WEIGHT_ESTIMATED
        ) {
            assertEquals(2.4, value)
            assertEquals("weight_estimated", settings.label)
        }
    }

    @Test
    fun testWeightMeasured() {
        createHarvest().copy(
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    weightMeasured = 2.5
                )
            )
        ).assertCreatedField<DoubleField<CommonHarvestField>>(
            CommonHarvestField.WEIGHT_MEASURED
        ) {
            assertEquals(2.5, value)
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
        ).assertCreatedField<StringListField<CommonHarvestField>>(
            CommonHarvestField.FITNESS_CLASS
        ) {
            assertEquals(listOf(GameFitnessClass.EXCELLENT.ordinal.toLong()), selected)
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
        ).assertCreatedField<StringListField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_TYPE
        ) {
            assertEquals(listOf(GameAntlersType.CERVINE.ordinal.toLong()), selected)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_WIDTH
        ) {
            assertEquals(37, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLER_POINTS_LEFT
        ) {
            assertEquals(12, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLER_POINTS_RIGHT
        ) {
            assertEquals(15, value)
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
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_LOST
        ) {
            assertEquals(true, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_GIRTH
        ) {
            assertEquals(15, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLER_SHAFT_WIDTH
        ) {
            assertEquals(12, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_LENGTH
        ) {
            assertEquals(10, value)
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
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ANTLERS_INNER_WIDTH
        ) {
            assertEquals(10, value)
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
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.ALONE
        ) {
            assertEquals(true, value)
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
    fun testSelectPermitWhenNoPermit() {
        createHarvest().copy().assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.SELECT_PERMIT
        ) {
            assertEquals(false, value)
            assertEquals("select_permit", settings.label)
        }
    }

    @Test
    fun testSelectPermitWhenPermitSelected() {
        createHarvest().copy(
            permitType = "type",
            permitNumber = "123"
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.SELECT_PERMIT
        ) {
            assertEquals(true, value)
            assertEquals("select_permit", settings.label)
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
        ).assertCreatedField<LabelField<CommonHarvestField>>(
            CommonHarvestField.PERMIT_INFORMATION
        ) {
            assertEquals("type\n123", text)
            assertEquals(LabelField.Type.LINK, type)
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
        ).assertCreatedField<LabelField<CommonHarvestField>>(
            CommonHarvestField.PERMIT_INFORMATION
        ) {
            assertEquals("mockPermit\n123", text)
            assertEquals(LabelField.Type.LINK, type)
        }
    }

    @Test
    fun testFeedingPlace() {
        createHarvest().copy(
            feedingPlace = true,
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE
        ) {
            assertEquals(true, value)
            assertEquals("feeding_place", settings.label)
        }
    }

    @Test
    fun testHuntingMethod() {
        createHarvest().copy(
            greySealHuntingMethod = GreySealHuntingMethod.CAPTURED_ALIVE.toBackendEnum(),
        ).assertCreatedField<StringListField<CommonHarvestField>>(
            CommonHarvestField.GREY_SEAL_HUNTING_METHOD
        ) {
            assertEquals(listOf(GreySealHuntingMethod.CAPTURED_ALIVE.ordinal.toLong()), selected)
            assertEquals("grey_seal_hunting_method", settings.label)
        }
    }

    @Test
    fun testTaigaBeanGoose() {
        createHarvest().copy(
            taigaBeanGoose = false,
        ).assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.IS_TAIGA_BEAN_GOOSE
        ) {
            assertEquals(false, value)
            assertEquals("is_taiga_bean_goose", settings.label)
        }
    }

    @Test
    fun testOwnHarvest() {
        createHarvest().assertCreatedField<BooleanField<CommonHarvestField>>(
            CommonHarvestField.OWN_HARVEST
        ) {
            assertEquals(true, value)
            assertEquals("own_harvest", settings.label)
        }
    }

    @Test
    fun testActor() {
        createHarvest().copy(
            actorInfo = GroupHuntingPerson.Guest(PersonWithHunterNumber(
                id = 666,
                rev = 1,
                byName = "Matti",
                lastName = "Meikäläinen",
                hunterNumber = "22222222"
            )),
        ).assertCreatedField<StringListField<CommonHarvestField>>(
            CommonHarvestField.ACTOR
        ) {
            assertEquals(
                setOf(
                    StringWithId(
                        id = GroupHuntingPerson.SearchingByHunterNumber.ID,
                        string = "other_hunter"
                    ), StringWithId(id = 666, string = "Matti Meikäläinen")
                ), values.toSet()
            )
            assertEquals("actor", settings.label)
        }
    }

    @Test
    fun testActorHunterNumber() {
        createHarvest().copy(
            actorInfo = GroupHuntingPerson.Guest(PersonWithHunterNumber(
                id = 666,
                rev = 1,
                byName = "Matti",
                lastName = "Meikäläinen",
                hunterNumber = "22222222"
            )),
        ).assertCreatedField<IntField<CommonHarvestField>>(
            CommonHarvestField.ACTOR_HUNTER_NUMBER
        ) {
            assertEquals(22222222, value)
            assertEquals("hunter_id", settings.label)
        }
    }

    @Test
    fun testActorHunterNumberInfoOrError() {
        createHarvest().copy(
            actorInfo = GroupHuntingPerson.SearchingByHunterNumber(
                hunterNumber = "83928476",
                status = GroupHuntingPerson.SearchingByHunterNumber.Status.INVALID_HUNTER_NUMBER
            ),
        ).assertCreatedField<LabelField<CommonHarvestField>>(
            CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR
        ) {
            assertEquals("invalid_hunter_id", text)
        }
    }

    @Test
    fun testNotSupportedFields() {
        val harvest = createHarvest()

        listOf(
            CommonHarvestField.AUTHOR,
            CommonHarvestField.HARVEST_REPORT_STATE,
        ).forEach { field ->
            HarvestReportingType.values().forEach { harvestReportingType ->
                val producedField = fieldProducer.createField(
                    fieldSpecification = field.noRequirement(),
                    harvest = harvest,
                    harvestReportingType = harvestReportingType,
                    shooters = emptyList(),
                    ownHarvest = true,
                )

                assertNull(producedField, "field $field")
            }
        }
    }

    private fun CommonHarvestData.assertStringField(
        field: CommonHarvestField,
        harvestReportingType: HarvestReportingType = HarvestReportingType.SEASON,
        readOnly: Boolean = false,
        fieldAssertions: StringField<CommonHarvestField>.() -> Unit
    ) {
        assertCreatedField<StringField<CommonHarvestField>>(field, harvestReportingType) {
            assertFalse(settings.singleLine, "field $field")
            assertEquals(readOnly, settings.readOnly, "field $field")

            fieldAssertions()
        }
    }

    private fun <FieldType: DataField<CommonHarvestField>> CommonHarvestData.assertCreatedField(
        field: CommonHarvestField,
        harvestReportingType: HarvestReportingType = HarvestReportingType.SEASON,
        fieldAssertions: FieldType.() -> Unit
    ) {
        assertCreatedField(
            fieldSpecification = field.noRequirement(),
            harvestReportingType = harvestReportingType,
            fieldAssertions = fieldAssertions,
        )
    }

    private fun <FieldType: DataField<CommonHarvestField>> CommonHarvestData.assertCreatedField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvestReportingType: HarvestReportingType = HarvestReportingType.SEASON,
        fieldAssertions: FieldType.() -> Unit
    ) {
        val producedField = fieldProducer.createField(
            fieldSpecification = fieldSpecification,
            harvest = this,
            harvestReportingType = harvestReportingType,
            shooters = emptyList(),
            ownHarvest = true,
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
            selectedClub = SearchableOrganization.Unknown,
            actorInfo = GroupHuntingPerson.Unknown,
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
