package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.ui.dataField.*
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
    fun testEditMooseGuestHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.voluntary(),
                includeActorHunterNumber = true,
            ),
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
                         groupMembers = listOf(), // no members, current actor must be guest
                     ))
    }

    @Test
    fun testEditMooseSearchingHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.required(),
                includeActorHunterNumber = true,
                includeActorHunterNumberInfoOrError = true,
            ),
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
                         actorInfoOverride = GroupHuntingPerson.SearchingByHunterNumber.startSearch(),
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

    private fun getCommonEditFields(
        actorHunterNumberRequirement: FieldRequirement = FieldRequirement.voluntary(),
        includeActorHunterNumber: Boolean = true,
        includeActorHunterNumberInfoOrError: Boolean = false,
    ): Array<FieldSpecification<GroupHarvestField>> {
        return listOfNotNull(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.HUNTING_DAY_AND_TIME.required(),
                GroupHarvestField.HEADLINE_SHOOTER.noRequirement(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.ACTOR_HUNTER_NUMBER
                    .withRequirement { actorHunterNumberRequirement }
                    .takeIf { includeActorHunterNumber },
                GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary()
                    .takeIf { includeActorHunterNumberInfoOrError },
                GroupHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
        ).toTypedArray()
    }
}
