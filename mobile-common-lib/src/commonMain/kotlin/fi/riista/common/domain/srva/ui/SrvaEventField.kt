package fi.riista.common.domain.srva.ui

import fi.riista.common.ui.dataField.IndexedDataFieldId

data class SrvaEventField(
    override val type: Type,
    override val index: Int = 0
) : IndexedDataFieldId<SrvaEventField.Type>() {

    init {
        validateIndex()
    }

    enum class Type {
        LOCATION,
        SPECIES_CODE,
        OTHER_SPECIES_DESCRIPTION,
        DATE_AND_TIME,
        APPROVER_OR_REJECTOR,
        SPECIMEN_AMOUNT,
        SPECIMEN,
        EVENT_CATEGORY,
        DEPORTATION_ORDER_NUMBER, // i.e. karkotusmääräyksen numero
        EVENT_TYPE,
        EVENT_OTHER_TYPE_DESCRIPTION,
        EVENT_TYPE_DETAIL,
        EVENT_OTHER_TYPE_DETAIL_DESCRIPTION,
        EVENT_RESULT,
        EVENT_RESULT_DETAIL,

        METHOD_HEADER,
        METHOD_ITEM, // for modify: single method, use index to identify exact method
        SELECTED_METHODS, // for viewing: all methods in a list
        OTHER_METHOD_DESCRIPTION,

        PERSON_COUNT,
        HOURS_SPENT,
        DESCRIPTION,
        ;

        fun toField(index: Int = 0) = SrvaEventField(type = this, index = index)
    }

    companion object {
        fun fromInt(value: Int): SrvaEventField? {
            return toIndexedField(value) { type : Type, index ->
                SrvaEventField(type, index)
            }
        }
    }
}