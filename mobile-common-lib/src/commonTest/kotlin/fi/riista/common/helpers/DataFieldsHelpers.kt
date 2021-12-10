package fi.riista.common.helpers

import fi.riista.common.ui.dataField.*
import kotlin.test.assertEquals
import kotlin.test.fail


internal fun <FieldId: DataFieldId> DataFields<FieldId>.getIntField(expectedIndex: Int, id: FieldId):
        IntField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getBooleanField(expectedIndex: Int, id: FieldId):
        BooleanField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getStringField(expectedIndex: Int, id: FieldId):
        StringField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getLabelField(expectedIndex: Int, id: FieldId):
        LabelField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getStringListField(expectedIndex: Int, id: FieldId):
        StringListField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getDateTimeField(expectedIndex: Int, id: FieldId):
        DateAndTimeField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getLocationField(expectedIndex: Int, id: FieldId):
        LocationField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getSpeciesCodeField(expectedIndex: Int, id: FieldId):
        SpeciesCodeField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getGenderField(expectedIndex: Int, id: FieldId):
        GenderField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getAgeField(expectedIndex: Int, id: FieldId):
        AgeField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getSelectDurationField(expectedIndex: Int, id: FieldId):
        SelectDurationField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getHuntingDayAndTimeField(expectedIndex: Int, id: FieldId):
        HuntingDayAndTimeField<FieldId> {
    return getField(expectedIndex, id)
}

internal inline fun <FieldId: DataFieldId, reified FieldType> DataFields<FieldId>.getField(
    expectedIndex: Int, id: FieldId
): FieldType where FieldType : DataField<FieldId> {
    val foundIndex = indexOfFirst { it.id == id }
    assertEquals(expectedIndex, foundIndex,
                 "Field $id at wrong index $foundIndex (expected $expectedIndex)")

    val field = get(foundIndex)
    @Suppress("UNCHECKED_CAST")
    return try {
        field as FieldType
    } catch (e: Throwable) {
        fail("Field $id type was ${field::class} instead of ${FieldType::class}")
    }
}
