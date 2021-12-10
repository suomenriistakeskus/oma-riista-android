package fi.riista.common.groupHunting.ui

import fi.riista.common.ui.dataField.DataFieldId

enum class GroupHarvestField : DataFieldId {
    SPECIES_CODE,
    // display/select date and time.
    DATE_AND_TIME,
    ERROR_DATE_NOT_WITHIN_GROUP_PERMIT,
    // display/select hunting day and time
    HUNTING_DAY_AND_TIME,
    ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
    LOCATION,
    GENDER,
    AGE,
    NOT_EDIBLE,
    ADDITIONAL_INFORMATION,
    ADDITIONAL_INFORMATION_INSTRUCTIONS,
    HEADLINE_SHOOTER,
    HEADLINE_SPECIMEN,
    WEIGHT, // not really used but HarvestSpecimen has a field for this information
    WEIGHT_ESTIMATED,
    WEIGHT_MEASURED,
    FITNESS_CLASS,
    ANTLERS_TYPE,
    ANTLERS_WIDTH,
    ANTLER_POINTS_LEFT,
    ANTLER_POINTS_RIGHT,
    ANTLERS_LOST,
    ANTLERS_GIRTH,
    ANTLER_SHAFT_WIDTH,
    ANTLERS_LENGTH,
    ANTLERS_INNER_WIDTH,
    ACTOR,
    ACTOR_HUNTER_NUMBER,
    ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
    AUTHOR,
    ALONE,
    ANTLER_INSTRUCTIONS,

    // Hunting method for e.g. white tailed deer
    DEER_HUNTING_TYPE,
    DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): GroupHarvestField? {
            return GroupHarvestField.values().getOrNull(value)
        }
    }
}
