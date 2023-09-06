package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.FieldRequirement
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
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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
    fun testEditWhiteTailedDeer() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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
    fun testEditWhiteTailedDeerGuestHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                includeDeerHuntingType = true,
                actorHunterNumberRequirement = FieldRequirement.voluntary(),
                includeActorHunterNumber = true,
            ),
            CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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
    fun testEditWhiteTailedDeerSearchingHunter() {
        val expectedFields = listOf(
            *getCommonEditFields(
                includeDeerHuntingType = true,
                actorHunterNumberRequirement = FieldRequirement.required(),
                includeActorHunterNumber = true,
                includeActorHunterNumberInfoOrError = true,
            ),
            CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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
    fun testViewWhiteTailedDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonViewFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                CommonHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
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
    fun testEditWhiteTailedDeerAdultMaleAntlersNotLost() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false),
                CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement(),
                CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                CommonHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
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
    fun testViewWhiteTailedDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonViewFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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
    fun testEditWhiteTailedDeerAdultMaleAntlersLost() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
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

    @Test
    fun testWhiteTailedDeerWithKnownHuntingTypes() {
        val expectedFields = listOf(
                *getCommonEditFields(includeDeerHuntingType = true),
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
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
                CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION.voluntary(),
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement(),
        )

        assertFields(expectedFields, getFields(
                speciesCode = speciesCode,
                mode = GroupHuntingHarvestFields.Context.Mode.EDIT,
                deerHuntingType = DeerHuntingType.OTHER
        ))
    }

}
