package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.domain.groupHunting.dto.toGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.toGroupHuntingHarvestData
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.util.deserializeFromJson
import kotlin.test.assertEquals

open class GroupHuntingHarvestFieldsTest {

    fun assertFields(expectedFields: List<FieldSpecification<GroupHarvestField>>,
                     fields: List<FieldSpecification<GroupHarvestField>>,
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
    ): List<FieldSpecification<GroupHarvestField>> {
        val harvest =
            MockGroupHuntingData.AcceptedHarvest.deserializeFromJson<GroupHuntingHarvestDTO>()!!
                .toGroupHuntingHarvest()!!
                .copy(
                        deerHuntingType = BackendEnum.create(deerHuntingType),
                        gameSpeciesCode = speciesCode,
                        specimens = listOf(
                                HarvestSpecimen(
                                        age = BackendEnum.create(age),
                                        gender = BackendEnum.create(gender),
                                        antlersLost = antlersLost
                                )
                        ),
                )

        val harvestData = harvest.toGroupHuntingHarvestData(groupMembers)
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