package fi.riista.common.groupHunting.ui.groupHarvest

import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary


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
        includeDeerHuntingOtherTypeDescription: Boolean = false
    ): Array<FieldSpecification<GroupHarvestField>> {
        return listOfNotNull(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.DATE_AND_TIME.required(),
                GroupHarvestField.HEADLINE_SHOOTER.noRequirement(),
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.DEER_HUNTING_TYPE.voluntary().takeIf { includeDeerHuntingType },
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary().takeIf { includeDeerHuntingOtherTypeDescription },
                GroupHarvestField.HEADLINE_SPECIMEN.noRequirement(),
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
        ).toTypedArray()
    }
}