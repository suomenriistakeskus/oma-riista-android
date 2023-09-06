package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.ui.dataField.IndexedDataFieldId

data class HuntingControlEventField(
    override val type: Type,
    override val index: Int = 0
) : IndexedDataFieldId<HuntingControlEventField.Type>() {

    init {
        validateIndex()
    }

    enum class Type {
        LOCATION,
        DATE,
        START_AND_END_TIME,
        START_TIME,
        END_TIME,
        DURATION,
        EVENT_TYPE,
        NUMBER_OF_INSPECTORS,
        INSPECTORS,
        INSPECTOR_NAMES,
        COOPERATION,
        OTHER_PARTICIPANTS,
        WOLF_TERRITORY,
        LOCATION_DESCRIPTION,
        EVENT_DESCRIPTION,
        NUMBER_OF_CUSTOMERS,
        NUMBER_OF_PROOF_ORDERS,
        HEADLINE_ATTACHMENTS,
        ERROR_NO_INSPECTORS_FOR_DATE,
        ERROR_NO_SELF_AS_INSPECTOR,
        ATTACHMENT,
        ADD_ATTACHMENT,
        ;

        fun toField(index: Int = 0) = HuntingControlEventField(type = this, index = index)
    }

    companion object {
        fun fromInt(value: Int): HuntingControlEventField? {
            return toIndexedField(value) { type: Type, index ->
                HuntingControlEventField(type, index)
            }
        }
    }
}
