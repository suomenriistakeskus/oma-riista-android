package fi.riista.common.domain.groupHunting.ui.groupSelection

import fi.riista.common.ui.dataField.DataFieldId

enum class SelectHuntingGroupField: DataFieldId {
    HUNTING_CLUB,
    SEASON,
    SPECIES,
    HUNTING_GROUP,
    PERMIT_INFORMATION,
    HUNTING_HAS_ENDED,
    ;

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): SelectHuntingGroupField? {
            return values().getOrNull(value)
        }
    }
}