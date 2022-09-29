package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.ui.dataField.*


open class DeerHarvestFieldsTest: GroupHuntingHarvestFieldsTest() {

    fun getCommonViewFields(
        includeDeerHuntingType: Boolean = false,
        includeDeerHuntingOtherTypeDescription: Boolean = false
    ): Array<FieldSpecification<GroupHarvestField>> {
        return listOfNotNull(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.DATE_AND_TIME.required(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.AUTHOR.required(),
                GroupHarvestField.DEER_HUNTING_TYPE.voluntary().takeIf { includeDeerHuntingType },
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary().takeIf { includeDeerHuntingOtherTypeDescription },
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
        ).toTypedArray()
    }

    fun getCommonEditFields(
        includeDeerHuntingType: Boolean = false,
        includeDeerHuntingOtherTypeDescription: Boolean = false,
        actorHunterNumberRequirement: FieldRequirement = FieldRequirement.voluntary(),
        includeActorHunterNumber: Boolean = true,
        includeActorHunterNumberInfoOrError: Boolean = false,
    ): Array<FieldSpecification<GroupHarvestField>> {
        return listOfNotNull(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.DATE_AND_TIME.required(),
                GroupHarvestField.HEADLINE_SHOOTER.noRequirement(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.ACTOR_HUNTER_NUMBER
                    .withRequirement { actorHunterNumberRequirement }
                    .takeIf { includeActorHunterNumber },
                GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary()
                    .takeIf { includeActorHunterNumberInfoOrError },
                GroupHarvestField.DEER_HUNTING_TYPE.voluntary().takeIf { includeDeerHuntingType },
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary().takeIf { includeDeerHuntingOtherTypeDescription },
                GroupHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
        ).toTypedArray()
    }
}