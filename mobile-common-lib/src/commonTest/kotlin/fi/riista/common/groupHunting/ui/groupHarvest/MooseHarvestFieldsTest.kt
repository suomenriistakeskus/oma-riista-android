package fi.riista.common.groupHunting.ui.groupHarvest

import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.model.GameAge
import fi.riista.common.model.Gender
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.MOOSE_ID

class MooseHarvestFieldsTest: GroupHuntingHarvestFieldsTest() {

    @Test
    fun testViewMoose() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
        )

        assertFields(expectedFields,
                     getFields(
                             speciesCode = speciesCode,
                             mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                     ))
    }

    @Test
    fun testEditMoose() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields,
                     getFields(
                             speciesCode = speciesCode,
                             mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                     ))
    }

    @Test
    fun testViewMooseYoung() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.ALONE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
        )

        assertFields(expectedFields,
                     getFields(
                             speciesCode = speciesCode,
                             mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                             age = GameAge.YOUNG,
                             gender = null
                     ), "gender = null")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                age = GameAge.YOUNG,
                gender = Gender.MALE
        ), "gender = male")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW,
                age = GameAge.YOUNG,
                gender = Gender.MALE
        ), "gender = female")
    }

    @Test
    fun testEditMooseYoung() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.ALONE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields,
                     getFields(
                             speciesCode = speciesCode,
                             mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                             age = GameAge.YOUNG,
                             gender = null
                     ), "gender = null")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.YOUNG,
                gender = Gender.MALE
        ), "gender = male")

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.YOUNG,
                gender = Gender.MALE
        ), "gender = female")
    }

    @Test
    fun testViewMooseAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLERS_TYPE.voluntary(),
                GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_GIRTH.voluntary(),
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
    fun testEditMooseAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                GroupHarvestField.ANTLERS_TYPE.voluntary(),
                GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                GroupHarvestField.ANTLERS_GIRTH.voluntary(),
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
    fun testViewMooseAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
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
    fun testEditMooseAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.FITNESS_CLASS.voluntary(),
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

    private fun getCommonViewFields(): Array<FieldSpecification<GroupHarvestField>> {
        return listOf(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.DATE_AND_TIME.required(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.AUTHOR.required(),
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
        ).toTypedArray()
    }

    private fun getCommonEditFields(): Array<FieldSpecification<GroupHarvestField>> {
        return listOf(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.HUNTING_DAY_AND_TIME.required(),
                GroupHarvestField.HEADLINE_SHOOTER.noRequirement(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.ACTOR_HUNTER_NUMBER.voluntary(),
                GroupHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
        ).toTypedArray()
    }
}
