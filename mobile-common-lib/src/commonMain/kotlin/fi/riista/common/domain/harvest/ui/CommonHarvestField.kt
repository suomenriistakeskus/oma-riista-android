package fi.riista.common.domain.harvest.ui

import fi.riista.common.ui.dataField.DataFieldId

enum class CommonHarvestField : DataFieldId {
    SELECT_PERMIT, // can be used for UI element that allows permit selection
    PERMIT_INFORMATION, // permit information
    PERMIT_REQUIRED_NOTIFICATION, // a notification regarding permit (e.g. "permit required")

    SPECIES_CODE, // for group hunting (no images displayed)
    SPECIES_CODE_AND_IMAGE, // for normal harvests that should also display an image

    HARVEST_REPORT_STATE,

    // display/select date and time.
    DATE_AND_TIME,
    ERROR_DATE_NOT_WITHIN_PERMIT,
    ERROR_DATETIME_IN_FUTURE,

    // display/select hunting day and time
    HUNTING_DAY_AND_TIME,
    ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
    LOCATION,

    // for species that allow multiple specimens on harvests
    SPECIMEN_AMOUNT,
    SPECIMENS,


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
    OWN_HARVEST,
    ACTOR,
    ACTOR_HUNTER_NUMBER,
    ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
    AUTHOR,
    SELECTED_CLUB,
    SELECTED_CLUB_OFFICIAL_CODE,
    SELECTED_CLUB_OFFICIAL_CODE_INFO_OR_ERROR,
    ALONE,
    ANTLER_INSTRUCTIONS,

    // Hunting method for e.g. white tailed deer
    DEER_HUNTING_TYPE,
    DEER_HUNTING_OTHER_TYPE_DESCRIPTION,

    WILD_BOAR_FEEDING_PLACE,
    GREY_SEAL_HUNTING_METHOD,
    IS_TAIGA_BEAN_GOOSE,

    // description visible only to the user
    DESCRIPTION,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): CommonHarvestField? {
            return values().getOrNull(value)
        }
    }
}
