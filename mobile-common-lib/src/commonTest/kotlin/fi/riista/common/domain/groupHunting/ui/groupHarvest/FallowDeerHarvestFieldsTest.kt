package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test

private const val speciesCode = SpeciesCodes.FALLOW_DEER_ID

class FallowDeerHarvestFieldsTest: DeerHarvestFieldsTest() {

    @Test
    fun testViewFallowDeer() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
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
    fun testEditFallowDeer() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
    fun testEditFallowDeerGuestHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.voluntary(),
                includeActorHunterNumber = true,
            ),
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
    fun testEditFallowDeerSearchingHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                actorHunterNumberRequirement = FieldRequirement.required(),
                includeActorHunterNumber = true,
                includeActorHunterNumberInfoOrError = true,
            ),
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
    fun testViewFallowDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
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
    fun testEditFallowDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
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
    fun testViewFallowDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
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
    fun testEditFallowDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
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
}
