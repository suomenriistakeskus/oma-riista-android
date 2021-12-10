package fi.riista.common.groupHunting.ui.groupHarvest

import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.model.GameAge
import fi.riista.common.model.Gender
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.ROE_DEER_ID

class RoeDeerHarvestFieldsTest: DeerHarvestFieldsTest() {

    @Test
    fun testViewRoeDeer() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
        )

        assertFields(
                expectedFields,
                getFields(
                        speciesCode = speciesCode,
                        mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                ),
                "view"
        )
    }

    @Test
    fun testEditRoeDeer() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
        )

        assertFields(
                expectedFields,
                getFields(
                        speciesCode = speciesCode,
                        mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                ),
                "view"
        )
    }

    @Test
    fun testViewRoeDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                GroupHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = null
        ), message = "antlersLost = null")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = false
        ), message = "antlersLost = false")
    }

    @Test
    fun testEditRoeDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                GroupHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = null
        ), message = "antlersLost = null")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = false
        ), message = "antlersLost = false")
    }

    @Test
    fun testViewRoeDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = true
        ))
    }

    @Test
    fun testEditRoeDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = true
        ))
    }
}
