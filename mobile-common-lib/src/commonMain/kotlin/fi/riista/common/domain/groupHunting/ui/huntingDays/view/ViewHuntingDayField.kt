package fi.riista.common.domain.groupHunting.ui.huntingDays.view

import fi.riista.common.ui.dataField.DataFieldId

data class ViewHuntingDayField(
    val type: Type,
    val id: Long = 0,
): DataFieldId {

    enum class Type {
        // pieces of data
        START_DATE_AND_TIME,
        END_DATE_AND_TIME,
        NUMBER_OF_HUNTERS,
        HUNTING_METHOD,
        NUMBER_OF_HOUNDS,
        SNOW_DEPTH,
        BREAK_DURATION,

        // generic headers
        SECTION_HEADER,

        // all harvests with same types but with different ids
        HARVEST,
        OBSERVATION,

        // hunting day doesn't exist yet and UI should be displayed
        // indicating the situation + allowing day creation
        ACTION_CREATE_HUNTING_DAY,
    }

    override fun toInt(): Int {
        return hashCode()
    }
}
