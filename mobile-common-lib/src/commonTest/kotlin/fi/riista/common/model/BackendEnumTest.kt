package fi.riista.common.model

import fi.riista.common.resources.*
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BackendEnumTest {

    enum class TestEnum(
        override val rawBackendEnumValue: String,
        override val resourcesStringId: RStringId
    ) : RepresentsBackendEnum, LocalizableEnum {
        FOO("FOO", RR.string.group_hunting_day_label_number_of_hunters),
        BAR("BAR", RR.string.group_hunting_day_label_snow_depth_centimeters),
    }

    @Test
    fun testWrappingAndObtainingKnownValues() {
        val foo = "FOO".toTestEnum()
        assertEquals(TestEnum.FOO, foo.value)
        assertEquals("FOO", foo.rawBackendEnumValue)
        val bar = "BAR".toTestEnum()
        assertEquals(TestEnum.BAR, bar.value)
        assertEquals("BAR", bar.rawBackendEnumValue)
    }

    @Test
    fun testWrappingAnUnknownValue() {
        val unknownValue = "FOOBAR".toTestEnum()
        assertNull(unknownValue.value)
        assertEquals("FOOBAR", unknownValue.rawBackendEnumValue)
    }

    @Test
    fun testUpdatingUnknownValueToKnownValue() {
        val unknownValue = "FOOBAR".toTestEnum()
        assertNull(unknownValue.value)
        assertEquals("FOOBAR", unknownValue.rawBackendEnumValue)

        val foo = unknownValue.create(TestEnum.FOO)
        assertEquals(TestEnum.FOO, foo.value)
        assertEquals("FOO", foo.rawBackendEnumValue)
    }

    @Test
    fun testCreatingStringWithIdFromKnownValues() {
        val foo = BackendEnum.create(TestEnum.FOO)
        val fooStringWithId = foo.toLocalizedStringWithId(getStringProvider())
        assertEquals(0, fooStringWithId.id)
        assertEquals("number_of_hunters", fooStringWithId.string)

        val bar = BackendEnum.create(TestEnum.BAR)
        val barStringWithId = bar.toLocalizedStringWithId(getStringProvider())
        assertEquals(1, barStringWithId.id)
        assertEquals("snowdepth", barStringWithId.string)
    }

    @Test
    fun testCreatingStringWithIdFromEmptyBackendEnum() {
        val empty = BackendEnum.create<TestEnum>(null)
        with (empty.toLocalizedStringWithId(getStringProvider())) {
            assertEquals(-1, id)
            assertEquals("", string)

            assertEquals(StringWithId.emptyBackendEnumValue.id, id)
            assertEquals(StringWithId.emptyBackendEnumValue.string, string)
        }
    }

    @Test
    fun testCreatingStringWithIdFromUnknownBackendEnum() {
        val unknown = "UNKNOWN".toBackendEnum<TestEnum>()
        with (unknown.toLocalizedStringWithId(getStringProvider())) {
            assertEquals(-2, id)
            assertEquals("UNKNOWN", string)
        }
    }

    @Test
    fun testCreatingEnumWithValidStringAndWithValidId() {
        assertEquals(
                TestEnum.FOO,
                StringWithId("number_of_hunters", 0).toBackendEnum<TestEnum>().value
        )
        assertEquals(
                TestEnum.BAR,
                StringWithId("snowdepth", 1).toBackendEnum<TestEnum>().value
        )
    }

    @Test
    fun testCreatingEnumWithValidId() {
        // string value should not matter
        assertEquals(
                TestEnum.FOO,
                StringWithId("foobar", 0).toBackendEnum<TestEnum>().value
        )
        assertEquals(
                TestEnum.BAR,
                StringWithId("barfoo", 1).toBackendEnum<TestEnum>().value
        )
    }

    @Test
    fun testCreatingEnumFromUnknownBackendEnumValue() {
        // -2 --> unknown enum value, rawBackendEnumValue exists
        val backendEnum = StringWithId("unknown", -2).toBackendEnum<TestEnum>()
        assertNull(backendEnum.value)
        assertEquals("unknown", backendEnum.rawBackendEnumValue)
    }

    @Test
    fun testCreatingEnumFromEmptyBackendEnumValue() {
        val backendEnum = StringWithId.emptyBackendEnumValue.toBackendEnum<TestEnum>()
        assertNull(backendEnum.value)
        assertNull(backendEnum.rawBackendEnumValue)
    }

    @Test
    fun testFooSerializationToJson() {
        assertEquals("{\"rawBackendEnumValue\":\"FOO\",\"enumValues\":[\"FOO\",\"BAR\"]}",
                     TestEnum.FOO.toBackendEnum().serializeToJson())
    }

    @Test
    fun testFooDeserializationFromJson() {
        assertEquals(TestEnum.FOO.toBackendEnum(),
                     "{\"rawBackendEnumValue\":\"FOO\",\"enumValues\":[\"FOO\",\"BAR\"]}"
                         .deserializeFromJson<BackendEnum<TestEnum>>())
    }

    @Test
    fun testNullSerializationToJson() {
        assertEquals("{\"rawBackendEnumValue\":null,\"enumValues\":[\"FOO\",\"BAR\"]}",
                     BackendEnum.create<TestEnum>(null).serializeToJson())
    }

    @Test
    fun testNullDeserializationFromJson() {
        assertEquals(BackendEnum.create<TestEnum>(null),
                     "{\"rawBackendEnumValue\":null,\"enumValues\":[\"FOO\",\"BAR\"]}"
                         .deserializeFromJson<BackendEnum<TestEnum>>())
    }

    private fun getStringProvider(): StringProvider {
        return object : StringProvider {
            override fun getString(stringId: RStringId): String {
                return when (stringId) {
                    RR.string.group_hunting_day_label_number_of_hunters -> "number_of_hunters"
                    RR.string.group_hunting_day_label_snow_depth_centimeters -> "snowdepth"
                    else -> throw RuntimeException("Unexpected stringId ($stringId) requested")
                }
            }

            override fun getFormattedString(stringId: RStringId, arg: String): String {
                throw RuntimeException("Unexpected stringId ($stringId) requested")
            }
        }
    }

    private fun String.toTestEnum(): BackendEnum<TestEnum> = this.toBackendEnum()
}