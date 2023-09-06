package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.FieldRequirement
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
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
    fun testEditRoeDeerGuestHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.voluntary(),
                includeActorHunterNumber = true,
            ),
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
        )

        assertFields(
            expectedFields,
            getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                groupMembers = listOf(), // no members, current actor must be guest
            ),
            "edit guest"
        )
    }

    @Test
    fun testEditRoeDeerSearchingHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.required(),
                includeActorHunterNumber = true,
                includeActorHunterNumberInfoOrError = true,
            ),
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
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
    fun testViewRoeDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                CommonHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                CommonHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
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
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
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
