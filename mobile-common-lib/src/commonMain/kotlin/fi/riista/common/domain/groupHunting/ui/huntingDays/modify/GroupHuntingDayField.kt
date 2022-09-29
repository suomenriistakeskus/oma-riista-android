package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.ui.dataField.DataFieldId

enum class GroupHuntingDayField: DataFieldId {
    START_DATE_AND_TIME,
    END_DATE_AND_TIME,
    DATE_TIME_ERROR,
    NUMBER_OF_HUNTERS,
    HUNTING_METHOD,
    NUMBER_OF_HOUNDS,
    SNOW_DEPTH,
    BREAK_DURATION,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): GroupHuntingDayField? {
            return values().getOrNull(value)
        }
    }
}