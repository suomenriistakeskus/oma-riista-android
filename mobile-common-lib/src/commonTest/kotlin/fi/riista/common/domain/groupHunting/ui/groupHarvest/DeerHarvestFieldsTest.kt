package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.*


open class DeerHarvestFieldsTest: GroupHuntingHarvestFieldsTest() {

    fun getCommonViewFields(
        includeDeerHuntingType: Boolean = false,
        includeDeerHuntingOtherTypeDescription: Boolean = false
    ): Array<FieldSpecification<CommonHarvestField>> {
        return listOfNotNull(
                CommonHarvestField.LOCATION.required(),
                CommonHarvestField.SPECIES_CODE.required(),
                CommonHarvestField.DATE_AND_TIME.required(),
                CommonHarvestField.ACTOR.required(),
                CommonHarvestField.AUTHOR.required(),
                CommonHarvestField.DEER_HUNTING_TYPE.voluntary().takeIf { includeDeerHuntingType },
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary().takeIf { includeDeerHuntingOtherTypeDescription },
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
        ).toTypedArray()
    }

    fun getCommonEditFields(
        includeDeerHuntingType: Boolean = false,
        includeDeerHuntingOtherTypeDescription: Boolean = false,
        actorHunterNumberRequirement: FieldRequirement = FieldRequirement.voluntary(),
        includeActorHunterNumber: Boolean = true,
        includeActorHunterNumberInfoOrError: Boolean = false,
    ): Array<FieldSpecification<CommonHarvestField>> {
        return listOfNotNull(
                CommonHarvestField.LOCATION.required(),
                CommonHarvestField.SPECIES_CODE.required(),
                CommonHarvestField.DATE_AND_TIME.required(),
                CommonHarvestField.HEADLINE_SHOOTER.noRequirement(),
                CommonHarvestField.ACTOR.required(),
                CommonHarvestField.ACTOR_HUNTER_NUMBER
                    .withRequirement { actorHunterNumberRequirement }
                    .takeIf { includeActorHunterNumber },
                CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary()
                    .takeIf { includeActorHunterNumberInfoOrError },
                CommonHarvestField.DEER_HUNTING_TYPE.voluntary().takeIf { includeDeerHuntingType },
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary().takeIf { includeDeerHuntingOtherTypeDescription },
                CommonHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
        ).toTypedArray()
    }
}