package fi.riista.common.domain.harvest.ui

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context.Mode
import fi.riista.common.domain.harvest.ui.fields.SpeciesSpecificHarvestFieldsPre2020
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpeciesSpecificHarvestFieldsPre2020Test {

    private val speciesSpecificFields = SpeciesSpecificHarvestFieldsPre2020(
        speciesResolver = TestSpeciesResolver()
    )

    @Test
    fun testViewMoose() {
        val context = createContext(
            speciesCode = SpeciesCodes.MOOSE_ID,
            gender = null,
            age = null,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.NOT_EDIBLE.voluntary(),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                    CommonHarvestField.FITNESS_CLASS.voluntary(),
                    CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                ),
                actual = this
            )
        }
    }

    @Test
    fun testViewYoungMoose() {
        val context = createContext(
            speciesCode = SpeciesCodes.MOOSE_ID,
            gender = null,
            age = GameAge.YOUNG,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.NOT_EDIBLE.voluntary(),
                    CommonHarvestField.ALONE.voluntary(),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                    CommonHarvestField.FITNESS_CLASS.voluntary(),
                    CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                ),
                actual = this,
            )
        }
    }

    @Test
    fun testViewAdultMaleMoose() {
        val context = createContext(
            speciesCode = SpeciesCodes.MOOSE_ID,
            gender = Gender.MALE,
            age = GameAge.ADULT,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.NOT_EDIBLE.voluntary(),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                    CommonHarvestField.FITNESS_CLASS.voluntary(),
                    CommonHarvestField.ANTLERS_TYPE.voluntary(),
                    CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                ),
                actual = this
            )
        }
    }

    @Test
    fun testViewDeer() {
        SpeciesCodes.DEER_ANIMALS.forEach { speciesCode ->
            val context = createContext(
                speciesCode = speciesCode,
                gender = null,
                age = null,
                mode = Mode.VIEW,
            )

            with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.GENDER.required(),
                        CommonHarvestField.AGE.required(),
                        CommonHarvestField.NOT_EDIBLE.voluntary(),
                        CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                        CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                        CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                    ),
                    actual = this,
                    message = "species $speciesCode"
                )
            }
        }
    }

    @Test
    fun testViewAdultMaleDeer() {
        SpeciesCodes.DEER_ANIMALS.forEach { speciesCode ->
            val context = createContext(
                speciesCode = speciesCode,
                gender = Gender.MALE,
                age = GameAge.ADULT,
                mode = Mode.VIEW,
            )

            with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.GENDER.required(),
                        CommonHarvestField.AGE.required(),
                        CommonHarvestField.NOT_EDIBLE.voluntary(),
                        CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                        CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                        CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                        CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                    ),
                    actual = this,
                    message = "species $speciesCode"
                )
            }
        }
    }

    @Test
    fun testViewWildBoar() {
        val context = createContext(
            speciesCode = SpeciesCodes.WILD_BOAR_ID,
            gender = null,
            age = null,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.WILD_BOAR_FEEDING_PLACE.voluntary(),
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.WEIGHT.required(),
                ),
                actual = this,
            )
        }
    }

    @Test
    fun testViewGreySeal() {
        listOf(
            null,
            GreySealHuntingMethod.SHOT,
            GreySealHuntingMethod.CAPTURED_ALIVE
        ).forEach { greySealHuntingMethod ->
            val context = createContext(
                speciesCode = SpeciesCodes.GREY_SEAL_ID,
                gender = null,
                age = null,
                greySealHuntingMethod = greySealHuntingMethod,
                mode = Mode.VIEW,
            ).copy(
                harvestReportingType = HarvestReportingType.BASIC
            )

            with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    // all voluntary for BASIC reporting
                    expected = listOf(
                        CommonHarvestField.GREY_SEAL_HUNTING_METHOD.voluntary(),
                        CommonHarvestField.GENDER.voluntary(),
                        CommonHarvestField.AGE.voluntary(),
                        CommonHarvestField.WEIGHT.voluntary(),
                    ),
                    actual = this,
                )
            }
        }
    }

    @Test
    fun testViewGreySealShotButLost() {
        val context = createContext(
            speciesCode = SpeciesCodes.GREY_SEAL_ID,
            gender = null,
            age = null,
            greySealHuntingMethod = GreySealHuntingMethod.SHOT_BUT_LOST,
            mode = Mode.VIEW,
        ).copy(
            harvestReportingType = HarvestReportingType.BASIC
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                // all voluntary for BASIC reporting
                expected = listOf(
                    CommonHarvestField.GREY_SEAL_HUNTING_METHOD.voluntary(),
                    CommonHarvestField.GENDER.voluntary(),
                    CommonHarvestField.AGE.voluntary(),
                ),
                actual = this,
            )
        }
    }

    @Test
    fun testViewGreySealWithNonBasicReportingType() {
        listOf(
            HarvestReportingType.PERMIT,
            HarvestReportingType.SEASON
        ).forEach { harvestReportingType ->
            val context = createContext(
                speciesCode = SpeciesCodes.GREY_SEAL_ID,
                gender = null,
                age = null,
                mode = Mode.VIEW,
            ).copy(
                harvestReportingType = harvestReportingType
            )

            with(speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.GREY_SEAL_HUNTING_METHOD.required(),
                        CommonHarvestField.GENDER.required(),
                        CommonHarvestField.AGE.required(),
                        CommonHarvestField.WEIGHT.required(),
                    ),
                    actual = this,
                )
            }
        }
    }

    @Test
    fun testViewBeanGoose() {
        val context = createContext(
            speciesCode = SpeciesCodes.BEAN_GOOSE_ID,
            gender = null,
            age = null,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.SPECIMEN_AMOUNT.required(),
                    CommonHarvestField.SPECIMENS.required(),
                ),
                actual = this,
            )
        }
    }

    @Test
    fun testViewBeanGooseWithNonBasicReportingType() {
        listOf(
            HarvestReportingType.PERMIT,
            HarvestReportingType.SEASON
        ).forEach { harvestReportingType ->
            val context = createContext(
                speciesCode = SpeciesCodes.BEAN_GOOSE_ID,
                gender = null,
                age = null,
                mode = Mode.VIEW,
            ).copy(
                harvestReportingType = harvestReportingType
            )

            with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.IS_TAIGA_BEAN_GOOSE.voluntary(),
                        CommonHarvestField.SPECIMEN_AMOUNT.required(),
                        CommonHarvestField.SPECIMENS.required(),
                    ),
                    actual = this,
                )
            }
        }
    }

    @Test
    fun testSpeciesAllowingMultipleSpecimens() {
        TestSpeciesResolver.speciesAllowingMultipleSpecimen.forEach { speciesCode ->
            val context = createContext(
                speciesCode = speciesCode,
                gender = null,
                age = null,
                mode = Mode.VIEW,
            )

            with (speciesSpecificFields.defaultSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.SPECIMEN_AMOUNT.required(),
                        CommonHarvestField.SPECIMENS.required(),
                    ),
                    actual = this,
                    message = "species $speciesCode"
                )
            }
        }
    }

    @Test
    fun testSpeciesAllowingOneSpecimen() {
        listOf(
            47348, 46615, 47503, 47507, 50106, 50386, 48089, 48251, 48250, 48537, 50336, 46549, 46542, 46587, 47329,
            47230, 47240, 47169, 47223, 47243, 47212, 200555, 47305, 47282, 47926, 47484, 47476, 47479, 47629, 200556,
            47774, 53004,
        ).forEach { speciesCode ->
            assertFalse(TestSpeciesResolver.INSTANCE.getMultipleSpecimensAllowedOnHarvests(speciesCode),
                "species $speciesCode")

            val context = createContext(
                speciesCode = speciesCode,
                gender = null,
                age = null,
                mode = Mode.VIEW,
            )

            with (speciesSpecificFields.defaultSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.GENDER,
                        CommonHarvestField.AGE,
                        CommonHarvestField.WEIGHT,
                    ),
                    actual = this.map { it.fieldId },
                    message = "species $speciesCode"
                )
            }
        }
    }

    private fun createContext(
        speciesCode: SpeciesCode,
        gender: Gender?,
        age: GameAge?,
        greySealHuntingMethod: GreySealHuntingMethod? = null,
        mode: Mode,
    ): CommonHarvestFields.Context {
        val harvest = CommonHarvestFieldsTest.getHarvest(speciesCode, gender, age, greySealHuntingMethod)
        val fields = CommonHarvestFieldsTest.getHarvestFields(
            speciesCode = harvest.species.knownSpeciesCodeOrNull()!!
        )

        return fields.createContext(harvest, mode, true)
    }


}
