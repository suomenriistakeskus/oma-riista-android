package fi.riista.common.groupHunting.ui

import fi.riista.common.ui.dataField.DataFieldId

enum class GroupObservationField : DataFieldId {
    SPECIES_CODE,
    HUNTING_DAY_AND_TIME,
    ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
    DATE_AND_TIME,
    LOCATION,
    OBSERVATION_TYPE,
    ACTOR,
    ACTOR_HUNTER_NUMBER,
    ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
    AUTHOR,
    HEADLINE_SPECIMEN_DETAILS,
    MOOSELIKE_MALE_AMOUNT,
    MOOSELIKE_FEMALE_AMOUNT,
    MOOSELIKE_FEMALE_1CALF_AMOUNT,
    MOOSELIKE_FEMALE_2CALF_AMOUNT,
    MOOSELIKE_FEMALE_3CALF_AMOUNT,
    MOOSELIKE_FEMALE_4CALF_AMOUNT,
    MOOSELIKE_CALF_AMOUNT,
    MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): GroupObservationField? {
            return values().getOrNull(value)
        }
    }
}
