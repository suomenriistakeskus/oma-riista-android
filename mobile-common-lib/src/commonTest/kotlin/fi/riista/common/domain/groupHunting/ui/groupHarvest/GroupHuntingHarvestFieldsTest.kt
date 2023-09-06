package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.domain.groupHunting.dto.toGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.toCommonHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.util.deserializeFromJson
import kotlin.test.assertEquals

open class GroupHuntingHarvestFieldsTest {

    fun assertFields(expectedFields: List<FieldSpecification<CommonHarvestField>>,
                     fields: List<FieldSpecification<CommonHarvestField>>,
                     message: String? = null
    ) {
        val expectedCount = expectedFields.size
        val foundCount = fields.size

        for (i in 0..maxOf(expectedCount, foundCount)) {
            val expectedField = expectedFields.getOrNull(i)
            val foundField = fields.getOrNull(i)

            assertEquals(expectedField?.fieldId, foundField?.fieldId,
                         "Field id mismatch at index $i (${message ?: "<no context>"})")

            assertEquals(expectedField?.requirementStatus, foundField?.requirementStatus,
                         "Field requirement mismatch for field ${expectedField?.fieldId} " +
                                 "(${message ?: "<no context>"})")
        }
    }

    fun getFields(speciesCode: SpeciesCode,
                  mode: GroupHuntingHarvestFields.Context.Mode,
                  age: GameAge? = null,
                  gender: Gender? = null,
                  antlersLost: Boolean? = null,
                  deerHuntingType: DeerHuntingType? = null,
                  actorInfoOverride: GroupHuntingPerson? = null,
                  groupMembers: List<HuntingGroupMember> = listOf(MockGroupHuntingData.HuntingGroupMember88888888)
    ): List<FieldSpecification<CommonHarvestField>> {
        val harvest =
            MockGroupHuntingData.AcceptedHarvest.deserializeFromJson<GroupHuntingHarvestDTO>()!!
                .toGroupHuntingHarvest()!!
                .copy(
                    deerHuntingType = BackendEnum.create(deerHuntingType),
                    gameSpeciesCode = speciesCode,
                    specimens = listOf(
                        CommonHarvestSpecimen(
                            id = null,
                            rev = null,
                            age = BackendEnum.create(age),
                            gender = BackendEnum.create(gender),
                            antlersLost = antlersLost,
                            weight = null,
                            weightEstimated = null,
                            weightMeasured = null,
                            fitnessClass = BackendEnum.create(null),
                            antlersType = BackendEnum.create(null),
                            antlersWidth = null,
                            antlerPointsLeft = null,
                            antlerPointsRight = null,
                            antlersGirth = null,
                            antlersLength = null,
                            antlersInnerWidth = null,
                            antlerShaftWidth = null,
                            notEdible = null,
                            alone = null,
                            additionalInfo = null,
                        )
                    ),
                )

        val harvestData = harvest.toCommonHarvestData(groupMembers)
            .let {
                when (actorInfoOverride) {
                    null -> it
                    else -> it.copy(actorInfo = actorInfoOverride)
                }
            }

        return GroupHuntingHarvestFields.getFieldsToBeDisplayed(
            context = GroupHuntingHarvestFields.Context(harvestData, mode)
        )
    }
}
