package fi.riista.common.domain.harvest.ui

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context.Mode
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MooseSpecificHarvestFieldsAfter2020Test: SpeciesSpecificHarvestFieldsAfter2020Test() {

    private val speciesCode = SpeciesCodes.MOOSE_ID

    @Test
    fun testView() {
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
                    CommonHarvestField.FITNESS_CLASS.voluntary(),
                    CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                ),
                actual = this
            )
        }
    }

    @Test
    fun testViewYoung() {
        val context = createContext(
            speciesCode = speciesCode,
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
                actual = this
            )
        }
    }

    @Test
    fun testViewAdultMale() {
        listOf(null, true).forEach { antlersLost ->
            val context = createContext(
                speciesCode = speciesCode,
                gender = Gender.MALE,
                age = GameAge.ADULT,
                antlersLost = antlersLost,
                mode = Mode.VIEW,
            )

            with(speciesSpecificFields.getSpeciesSpecificFields(context)) {
                assertEquals(
                    expected = listOf(
                        CommonHarvestField.GENDER.required(),
                        CommonHarvestField.AGE.required(),
                        CommonHarvestField.NOT_EDIBLE.voluntary(),
                        CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                        CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                        CommonHarvestField.FITNESS_CLASS.voluntary(),
                        CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = antlersLost == null),
                        CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                    ),
                    actual = this,
                    "antlers lost: $antlersLost"
                )
            }
        }
    }

    @Test
    fun testViewAdultMaleWithAntlersPresent() {
        val context = createContext(
            speciesCode = speciesCode,
            gender = Gender.MALE,
            age = GameAge.ADULT,
            antlersLost = false,
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
                    CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                    CommonHarvestField.ANTLERS_TYPE.voluntary(),
                    CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                    CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                ),
                actual = this
            )
        }
    }
}
