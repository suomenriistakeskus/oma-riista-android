package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DataFieldId

enum class DataFieldViewHolderType {
    LABEL_CAPTION,
    LABEL_ERROR,
    LABEL_INFO,
    SPECIES_NAME_AND_ICON,
    READONLY_DATE_AND_TIME,
    EDITABLE_DATE_AND_TIME,
    SELECT_HUNTING_DAY_AND_TIME,
    READONLY_HUNTING_DAY_AND_TIME,
    LOCATION_ON_MAP,
    GENDER,
    EDITABLE_GENDER,
    AGE,
    EDITABLE_AGE,
    READONLY_BOOLEAN_AS_RADIO_TOGGLE,
    EDITABLE_BOOLEAN_AS_RADIO_TOGGLE,
    READONLY_TEXT,
    READONLY_TEXT_SINGLE_LINE,
    EDITABLE_TEXT,
    EDITABLE_DOUBLE,
    SELECTABLE_STRING,
    INT,
    SELECTABLE_DURATION,
    INSTRUCTIONS,
    //TEXTFIELD_MULTILINE,
    ;

    val viewType: Int
        get() = ordinal

    companion object {
        fun fromViewType(viewType: Int): DataFieldViewHolderType {
            return values()[viewType]
        }
    }
}

interface DataFieldViewHolderTypeResolver<FieldId : DataFieldId> {
    fun resolveViewHolderType(dataField: DataField<FieldId>): DataFieldViewHolderType
}
