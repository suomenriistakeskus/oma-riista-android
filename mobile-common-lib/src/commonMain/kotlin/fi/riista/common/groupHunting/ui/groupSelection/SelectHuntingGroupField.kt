package fi.riista.common.groupHunting.ui.groupSelection

import fi.riista.common.ui.dataField.DataFieldId

enum class SelectHuntingGroupField: DataFieldId {
    HUNTING_CLUB,
    SEASON,
    SPECIES,
    HUNTING_GROUP,
    PERMIT_INFORMATION,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): SelectHuntingGroupField? {
            return values().getOrNull(value)
        }
    }
}