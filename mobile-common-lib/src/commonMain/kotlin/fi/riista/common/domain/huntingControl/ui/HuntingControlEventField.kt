package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.ui.dataField.DataFieldId

data class HuntingControlEventField(
    val type: Type,
    val index: Int = 0
) : DataFieldId {

    init {
        require(index in 0 until MAX_INDEX) {
            "index not between 0..$MAX_INDEX"
        }
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
        ATTACHMENT,
        ADD_ATTACHMENT,
        ;

        fun toField(index: Int = 0) = HuntingControlEventField(type = this, index = index)
    }

    override fun toInt() = MAX_INDEX * (type.ordinal + 1) + index

    companion object {
        private const val MAX_INDEX = 1000

        fun fromInt(value: Int): HuntingControlEventField? {
            val typeValue = (value / MAX_INDEX) - 1
            val indexValue =  value.mod(MAX_INDEX)
            return Type.values().getOrNull(typeValue)?.toField(indexValue)
        }
    }
}
