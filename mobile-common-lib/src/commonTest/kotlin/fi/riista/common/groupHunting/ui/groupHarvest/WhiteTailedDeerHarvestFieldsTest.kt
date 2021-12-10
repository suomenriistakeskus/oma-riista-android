package fi.riista.common.groupHunting.ui.groupHarvest

import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.model.DeerHuntingType
import fi.riista.common.model.GameAge
import fi.riista.common.model.Gender
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.WHITE_TAILED_DEER_ID

class WhiteTailedDeerHarvestFieldsTest: DeerHarvestFieldsTest() {

    @Test
    fun testViewWhiteTailedDeer() {
        val expectedFields = listOf(
                *getCommonViewFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
    fun testEditWhiteTailedDeer() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
    fun testViewWhiteTailedDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_GIRTH.voluntary(),
                GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                GroupHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
    fun testEditWhiteTailedDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_GIRTH.voluntary(),
                GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                GroupHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
    fun testViewWhiteTailedDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
    fun testEditWhiteTailedDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = true
        ))
    }

    @Test
    fun testWhiteTailedDeerWithKnownHuntingTypes() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                deerHuntingType = DeerHuntingType.DOG_HUNTING
        ))

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                deerHuntingType = DeerHuntingType.STAND_HUNTING
        ))
    }

    @Test
    fun testWhiteTailedDeerWithOtherHuntingTypes() {
        val expectedFields = listOf(
                *getCommonEditFields(
                        includeDeerHuntingType = true,
                        includeDeerHuntingOtherTypeDescription = true
                ),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                deerHuntingType = DeerHuntingType.OTHER
        ))
    }

}
