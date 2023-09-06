package fi.riista.common.domain.sun.ui

import fi.riista.common.ui.dataField.DataFieldId

enum class SunriseAndSunsetField : DataFieldId {
    LOCATION,
    DATE,

    TEXT_SUNRISE,
    TEXT_SUNSET,
    INSTRUCTIONS,
    DISCLAIMER,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): SunriseAndSunsetField? {
            return values().getOrNull(value)
        }
    }
}
