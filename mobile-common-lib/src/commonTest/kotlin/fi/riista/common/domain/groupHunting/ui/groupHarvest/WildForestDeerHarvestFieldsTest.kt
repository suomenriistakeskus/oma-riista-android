package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.WILD_FOREST_DEER_ID

class WildForestDeerHarvestFieldsTest: DeerHarvestFieldsTest() {

    @Test
    fun testViewWildForestDeer() {
        val expectedFields = listOf(
                *getCommonViewFields(),
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
    fun testEditWildForestDeer() {
        val expectedFields = listOf(
                *getCommonEditFields(),
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
                "edit"
        )
    }

    @Test
    fun testEditWildForestDeerGuestHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.voluntary(),
                includeActorHunterNumber = true,
            ),
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
                groupMembers = listOf(), // no members, current actor must be guest
            ),
            "edit"
        )
    }

    @Test
    fun testEditWildForestDeerSearchingHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.required(),
                includeActorHunterNumber = true,
                includeActorHunterNumberInfoOrError = true,
            ),
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
                actorInfoOverride = GroupHuntingPerson.SearchingByHunterNumber.startSearch(),
            ),
            "edit search hunter"
        )
    }

    @Test
    fun testViewWildForestDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
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
    fun testEditWildForestDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
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
    fun testViewWildForestDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
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
    fun testEditWildForestDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
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
}
