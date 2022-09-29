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

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getDateField(expectedIndex: Int, id: FieldId):
        DateField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getTimespanField(expectedIndex: Int, id: FieldId):
        TimespanField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getLocationField(expectedIndex: Int, id: FieldId):
        LocationField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getSpeciesField(expectedIndex: Int, id: FieldId):
        SpeciesField<FieldId> {
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

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getSpecimenField(expectedIndex: Int, id: FieldId):
        SpecimenField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getAttachmentField(expectedIndex: Int, id: FieldId):
        AttachmentField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getButtonField(expectedIndex: Int, id: FieldId):
        ButtonField<FieldId> {
    return getField(expectedIndex, id)
}

internal fun <FieldId: DataFieldId> DataFields<FieldId>.getChipField(expectedIndex: Int, id: FieldId):
        ChipField<FieldId> {
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
