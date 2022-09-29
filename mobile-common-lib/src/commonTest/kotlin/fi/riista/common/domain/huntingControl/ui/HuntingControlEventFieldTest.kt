package fi.riista.common.domain.huntingControl.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingControlEventFieldTest {
    @Test
    fun convertFieldToAndFromInt() {
        val field1 = HuntingControlEventField(type = HuntingControlEventField.Type.EVENT_TYPE, index = 0)
        assertEquals(field1, HuntingControlEventField.fromInt(field1.toInt()))

        val field2 = HuntingControlEventField(type = HuntingControlEventField.Type.EVENT_TYPE, index = 15)
        assertEquals(field2, HuntingControlEventField.fromInt(field2.toInt()))

        val field3 = HuntingControlEventField(type = HuntingControlEventField.Type.ATTACHMENT, index = 0)
        assertEquals(field3, HuntingControlEventField.fromInt(field3.toInt()))

        val field4 = HuntingControlEventField(type = HuntingControlEventField.Type.ATTACHMENT, index = 123)
        assertEquals(field4, HuntingControlEventField.fromInt(field4.toInt()))
    }
}
