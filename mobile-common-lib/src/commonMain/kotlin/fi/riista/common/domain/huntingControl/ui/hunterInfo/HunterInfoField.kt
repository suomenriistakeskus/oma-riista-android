package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.ui.dataField.IndexedDataFieldId

data class HunterInfoField(
    override val type: Type,
    override val index: Int = 0
) : IndexedDataFieldId<HunterInfoField.Type>() {

    init {
        validateIndex()
    }

    enum class Type {
        SCAN_QR_CODE,
        ENTER_HUNTER_NUMBER,
        ENTERED_SSN,
        HUNTER_NUMBER_INFO_OR_ERROR,
        RETRY_BUTTON,

        PERSONAL_DATA_HEADER,
        NAME,
        DATE_OF_BIRTH,
        HOME_MUNICIPALITY,
        HUNTER_NUMBER,

        HUNTING_LICENSE_HEADER,
        HUNTING_LICENSE_STATUS,
        HUNTING_LICENSE_DAY_OF_PAYMENT,

        SHOOTING_TEST_HEADER,
        SPECIES_CAPTION,
        SHOOTING_TEST_INFO,

        RESET_BUTTON,
        ;

        fun toField(index: Int = 0) = HunterInfoField(type = this, index = index)
    }

    companion object {
        fun fromInt(value: Int): HunterInfoField? {
            return toIndexedField(value) { type : Type, index ->
                HunterInfoField(type, index)
            }
        }
    }
}
