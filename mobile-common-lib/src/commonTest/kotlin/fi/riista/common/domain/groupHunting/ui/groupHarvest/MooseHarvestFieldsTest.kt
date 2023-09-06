package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import fi.riista.common.ui.dataField.withRequirement
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.MOOSE_ID

class MooseHarvestFieldsTest: GroupHuntingHarvestFieldsTest() {

    @Test
    fun testViewMoose() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            CommonHarvestField.FITNESS_CLASS.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            CommonHarvestField.FITNESS_CLASS.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
                CommonHarvestField.ALONE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
                CommonHarvestField.ALONE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                CommonHarvestField.ANTLERS_TYPE.voluntary(),
                CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                age = GameAge.ADULT,
                gender = Gender.MALE,
                antlersLost = true
        ))
    }

    private fun getCommonViewFields(): Array<FieldSpecification<CommonHarvestField>> {
        return listOf(
                CommonHarvestField.LOCATION.required(),
                CommonHarvestField.SPECIES_CODE.required(),
                CommonHarvestField.DATE_AND_TIME.required(),
                CommonHarvestField.ACTOR.required(),
                CommonHarvestField.AUTHOR.required(),
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
        ).toTypedArray()
    }

    private fun getCommonEditFields(
        actorHunterNumberRequirement: FieldRequirement = FieldRequirement.voluntary(),
        includeActorHunterNumber: Boolean = true,
        includeActorHunterNumberInfoOrError: Boolean = false,
    ): Array<FieldSpecification<CommonHarvestField>> {
        return listOfNotNull(
                CommonHarvestField.LOCATION.required(),
                CommonHarvestField.SPECIES_CODE.required(),
                CommonHarvestField.HUNTING_DAY_AND_TIME.required(),
                CommonHarvestField.HEADLINE_SHOOTER.noRequirement(),
                CommonHarvestField.ACTOR.required(),
                CommonHarvestField.ACTOR_HUNTER_NUMBER
                    .withRequirement { actorHunterNumberRequirement }
                    .takeIf { includeActorHunterNumber },
                CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary()
                    .takeIf { includeActorHunterNumberInfoOrError },
                CommonHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
        ).toTypedArray()
    }
}
