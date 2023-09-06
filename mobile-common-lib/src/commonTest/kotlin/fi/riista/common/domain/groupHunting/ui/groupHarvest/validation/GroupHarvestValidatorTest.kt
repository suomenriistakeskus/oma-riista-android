package fi.riista.common.domain.groupHunting.ui.groupHarvest.validation

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.domain.groupHunting.model.asGroupMember
import fi.riista.common.domain.groupHunting.ui.groupHarvest.GroupHuntingHarvestFields
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.toCommonSpecimenData
import fi.riista.common.domain.harvest.validation.CommonHarvestValidator
import fi.riista.common.domain.harvest.validation.CommonHarvestValidatorTest
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.GameAntlersType
import fi.riista.common.domain.model.GameFitnessClass
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.Entity
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.changeTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.MockDateTimeProvider
import fi.riista.common.util.generateMobileClientRefId
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Harvest validation from the group hunting perspective. Has partially same tests
 * as on [CommonHarvestValidatorTest]
 * */
class GroupHarvestValidatorTest {

    private val harvestValidator = GroupHarvestValidator(MOCK_DATE_TIME_PROVIDER, TestSpeciesResolver.INSTANCE)

    @Test
    fun testValidData() {
        val context = harvestContext()

        val displayedFields = GroupHuntingHarvestFields.getFieldsToBeDisplayed(context)
        val errors = harvestValidator.validate(
            context.harvest,
            createHuntingDays(),
            createPermit(),
            displayedFields
        )

        assertEquals(0, errors.size)
    }

    @Test
    fun testDateNotWithinPermit_Moose() {
        assertNoValidationErrors(
                harvest = createHarvest(),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                ),
                permit = createPermit(HARVEST_POINT_OF_TIME.date.copy(dayOfMonth = 15))
        )
    }

    @Test
    fun testDateNotWithinPermit_Deer() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.DATE_NOT_WITHIN_PERMIT,
                harvest = createHarvest(),
                speciesCodes = listOf(
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                ),
                permit = createPermit(HARVEST_POINT_OF_TIME.date.copy(dayOfMonth = 15))
        )
    }

    @Test
    fun testHuntingDay_Moose() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_HUNTING_DAY,
                harvest = createHarvest().copy(huntingDayId = null),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                )
        )
    }

    @Test
    fun testHuntingDay_Deer() {
        assertNoValidationErrors(
                harvest = createHarvest().copy(huntingDayId = null),
                speciesCodes = listOf(
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testTimeNotWithinHuntingDay_Moose() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.TIME_NOT_WITHIN_HUNTING_DAY,
                harvest = createHarvest(pointOfTime = HARVEST_POINT_OF_TIME.changeTime(5, 59, 59)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                )
        )
    }

    @Test
    fun testTimeNotWithinHuntingDay_Deer() {
        assertNoValidationErrors(
                harvest = createHarvest(pointOfTime = HARVEST_POINT_OF_TIME.changeTime(5, 59, 59)),
                speciesCodes = listOf(
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testActor() {
        assertValidationError(
            expectedError = CommonHarvestValidator.Error.MISSING_ACTOR,
            harvest = createHarvest().copy(actorInfo = GroupHuntingPerson.Unknown),
            speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )

        assertValidationError(
            expectedError = CommonHarvestValidator.Error.MISSING_ACTOR,
            harvest = createHarvest().copy(actorInfo = GroupHuntingPerson.SearchingByHunterNumber.startSearch()),
            speciesCodes = listOf(
                SpeciesCodes.MOOSE_ID,
                SpeciesCodes.FALLOW_DEER_ID,
                SpeciesCodes.ROE_DEER_ID,
                SpeciesCodes.WHITE_TAILED_DEER_ID,
                SpeciesCodes.WILD_FOREST_DEER_ID,
            )
        )
    }

    @Test
    fun testGender_null() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_GENDER,
                harvest = createHarvest(createSpecimen(gender = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testGender_UNKNOWN() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.INVALID_GENDER,
                harvest = createHarvest(createSpecimen(gender = Gender.UNKNOWN)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAge_null() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_AGE,
                harvest = createHarvest(createSpecimen(age = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAge_UNKNOWN() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.INVALID_AGE,
                harvest = createHarvest(createSpecimen(age = GameAge.UNKNOWN)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAlone_Moose() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_ALONE,
                harvest = createHarvest(createSpecimen(alone = null, age = GameAge.YOUNG)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                )
        )
    }

    @Test
    fun testAlone_Deer() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(alone = null, age = GameAge.YOUNG)),
                speciesCodes = listOf(
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testNotEdible_errors() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_NOT_EDIBLE,
                harvest = createHarvest(createSpecimen(notEdible = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testNotEdible_noErrors() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(notEdible = null)),
                speciesCodes = listOf(
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersLost() {
        assertValidationError(
                expectedError = CommonHarvestValidator.Error.MISSING_ANTLERS_LOST,
                harvest = createHarvest(createSpecimen(antlersLost = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }


    @Test
    fun testWeightEstimated() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(weightEstimated = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testWeightMeasured() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(weightMeasured = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testFitnessClass() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(fitnessClass = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersType() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlersType = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersWidth() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlersWidth = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlerPointsLeft() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlerPointsLeft = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlerPointsRight() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlerPointsRight = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersGirth() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlersGirth = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersLength() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlersLength = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlersInnerWidth() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlersInnerWidth = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    @Test
    fun testAntlerShaftWidth() {
        assertNoValidationErrors(
                harvest = createHarvest(createSpecimen(antlerShaftWidth = null)),
                speciesCodes = listOf(
                        SpeciesCodes.MOOSE_ID,
                        SpeciesCodes.FALLOW_DEER_ID,
                        SpeciesCodes.ROE_DEER_ID,
                        SpeciesCodes.WHITE_TAILED_DEER_ID,
                        SpeciesCodes.WILD_FOREST_DEER_ID,
                )
        )
    }

    private fun assertValidationError(
        expectedError: CommonHarvestValidator.Error,
        harvest: CommonHarvestData = createHarvest(),
        speciesCodes: List<SpeciesCode>,
        permit: HuntingGroupPermit = createPermit(),
        huntingDays: List<GroupHuntingDay> = createHuntingDays()
    ) {
        speciesCodes.forEach { speciesCode ->
            val errors = getValidationErrors(
                    harvest.copy(species = Species.Known(speciesCode)), permit, huntingDays)

            assertEquals(1, errors.size, "species: $speciesCode, errors: $errors")
            assertEquals(expectedError, errors[0], "species: $speciesCode")
        }
    }

    private fun assertNoValidationErrors(
        harvest: CommonHarvestData = createHarvest(),
        speciesCodes: List<SpeciesCode>,
        permit: HuntingGroupPermit = createPermit(),
        huntingDays: List<GroupHuntingDay> = createHuntingDays()
    ) {
        speciesCodes.forEach { speciesCode ->
            val errors = getValidationErrors(
                    harvest.copy(species = Species.Known(speciesCode)), permit, huntingDays)

            assertEquals(0, errors.size, "species: $speciesCode, errors: $errors")
        }
    }

    private fun getValidationErrors(
        harvest: CommonHarvestData,
        permit: HuntingGroupPermit = createPermit(),
        huntingDays: List<GroupHuntingDay>,
    ): List<CommonHarvestValidator.Error> {
        val context = harvestContext(harvest)

        val displayedFields = GroupHuntingHarvestFields.getFieldsToBeDisplayed(context)
        return harvestValidator.validate(
            harvest = context.harvest,
            huntingDays = huntingDays,
            huntingGroupPermit = permit,
            displayedFields = displayedFields,
        )
    }

    private fun harvestContext(harvest: CommonHarvestData = createHarvest()) =
        GroupHuntingHarvestFields.Context(harvest, GroupHuntingHarvestFields.Context.Mode.EDIT)

    private fun createPermit(date: LocalDate = HARVEST_POINT_OF_TIME.date): HuntingGroupPermit {
        return HuntingGroupPermit(
                permitNumber = "Permit",
                validityPeriods = listOf(
                        LocalDatePeriod(
                                beginDate = date,
                                endDate = date
                        )
                )
        )
    }

    private fun createHuntingDays(): List<GroupHuntingDay> {
        return listOf(GroupHuntingDay(
                id = GroupHuntingDayId.remote(HUNTING_DAY_ID),
                type = Entity.Type.REMOTE,
                rev = 0,
                huntingGroupId = 1,
                startDateTime = HARVEST_POINT_OF_TIME.changeTime(6, 0, 0),
                endDateTime = HARVEST_POINT_OF_TIME.changeTime(18, 0, 0),
                breakDurationInMinutes = 0,
                snowDepth = 0,
                huntingMethod = GroupHuntingMethodType.VAIJYNTA_KULKUPAIKOILLA.toBackendEnum(),
                numberOfHunters = 7,
                numberOfHounds = 2,
                createdBySystem = false
        ))
    }

    private fun createHarvest(
        specimen: CommonHarvestSpecimen? = createSpecimen(),
        species: Species = Species.Known(SpeciesCodes.MOOSE_ID),
        pointOfTime: LocalDateTime = HARVEST_POINT_OF_TIME
    ): CommonHarvestData {
        return CommonHarvestData(
            localId = null,
            localUrl = null,
            id = null,
            rev = null,
            species = species,
            location = ETRMSGeoLocation(
                latitude = 6000,
                longitude = 8000,
                source = fi.riista.common.model.GeoLocationSource.MANUAL.toBackendEnum(),
                accuracy = null,
                altitude = null,
                altitudeAccuracy = null,
            ).asKnownLocation(),
            pointOfTime = pointOfTime,
            description = null,
            images = EntityImages.noImages(),
            specimens = listOfNotNull(specimen).map { it.toCommonSpecimenData() },
            amount = null,
            actorInfo = PersonWithHunterNumber(
                id = 99,
                rev = 0,
                byName = "Pentti",
                lastName = "Mujunen",
                hunterNumber = "88888888",
            ).asGroupMember(),
            selectedClub = SearchableOrganization.Unknown,
            huntingDayId = GroupHuntingDayId.remote(HUNTING_DAY_ID),
            authorInfo = null,
            canEdit = true,
            modified = true,
            deleted = false,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = false,
            harvestReportState = BackendEnum.create(null),
            permitNumber = null,
            permitType = null,
            stateAcceptedToHarvestPermit = BackendEnum.create(null),
            deerHuntingType = BackendEnum.create(null),
            deerHuntingOtherTypeDescription = null,
            mobileClientRefId = generateMobileClientRefId(),
            harvestReportDone = false,
            rejected = false,
            feedingPlace = null,
            taigaBeanGoose = null,
            greySealHuntingMethod = BackendEnum.create(null),
        )
    }

    private fun createSpecimen(
        gender: Gender? = Gender.MALE,
        age: GameAge? = GameAge.ADULT,
        antlersLost: Boolean? = false,
        notEdible: Boolean? = false,
        alone: Boolean? = false,
        weightEstimated: Double? = 100.0,
        weightMeasured: Double? = 200.0,
        fitnessClass: GameFitnessClass? = GameFitnessClass.NORMAL,
        antlersType: GameAntlersType? = GameAntlersType.MIXED,
        antlersWidth: Int? = 150,
        antlerPointsLeft: Int? = 25,
        antlerPointsRight: Int? = 30,
        antlersGirth: Int? = 30,
        antlersLength: Int? = 60,
        antlersInnerWidth: Int? = 18,
        antlerShaftWidth: Int? = 20,
    ): CommonHarvestSpecimen {
        return CommonHarvestSpecimen(
            id = null,
            rev = null,
            gender = BackendEnum.create(gender),
            age = BackendEnum.create(age),
            antlersLost = antlersLost,
            notEdible = notEdible,
            alone = alone,
            weightEstimated = weightEstimated,
            weightMeasured = weightMeasured,
            fitnessClass = BackendEnum.create(fitnessClass),
            antlersType = BackendEnum.create(antlersType),
            antlersWidth = antlersWidth,
            antlerPointsLeft = antlerPointsLeft,
            antlerPointsRight = antlerPointsRight,
            antlersGirth = antlersGirth,
            antlersLength = antlersLength,
            antlersInnerWidth = antlersInnerWidth,
            antlerShaftWidth = antlerShaftWidth,
            additionalInfo = null,
            weight = null,
        )
    }

    companion object {
        private const val HUNTING_DAY_ID = 81L
        private val HARVEST_POINT_OF_TIME = LocalDateTime(2021, 6, 14, 14, 51, 45)

        private val MOCK_DATE_TIME_PROVIDER = MockDateTimeProvider(now = HARVEST_POINT_OF_TIME)
    }
}

