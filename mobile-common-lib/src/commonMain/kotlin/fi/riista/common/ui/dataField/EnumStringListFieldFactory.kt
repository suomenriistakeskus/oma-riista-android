package fi.riista.common.ui.dataField

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.StringWithId
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.*

/**
 * A factory that is able to create [StringListField]s based on given [BackendEnum]
 * (that wraps enums of type [E])
 */
internal class EnumStringListFieldFactory<E>(
    private val stringProvider: StringProvider,
    internal val allEnumValues: List<BackendEnum<E>>,
    private val enumToStringWithIdConverter: (BackendEnum<E>, StringProvider) -> StringWithId
) where E : Enum<E>, E : RepresentsBackendEnum {

    private val allLocalizedEnumValues: Map<BackendEnum<E>, StringWithId> by lazy {
        allEnumValues.associateWith { enumValue ->
            enumToStringWithIdConverter(enumValue, stringProvider)
        }
    }

    fun <DataId: DataFieldId> create(
        fieldId: DataId,
        currentEnumValue: BackendEnum<E>,
        allowEmptyValue: Boolean,
        configureFieldSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null,
    ): StringListField<DataId> {
        return create(
            fieldId = fieldId,
            currentEnumValue = currentEnumValue,
            enumValues = allEnumValues,
            allowEmptyValue = allowEmptyValue,
            configureFieldSettings = configureFieldSettings
        )
    }

    fun <DataId: DataFieldId> create(
        fieldId: DataId,
        currentEnumValue: BackendEnum<E>,
        enumValues: List<BackendEnum<E>>,
        allowEmptyValue: Boolean,
        configureFieldSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null,
    ): StringListField<DataId> {
        val selectedValue = enumToStringWithIdConverter(currentEnumValue, stringProvider)
        val values = mutableListOf<StringWithId>()

        // add an extra empty value if allowed _and_ selected value is not empty one
        if (allowEmptyValue && !selectedValue.isEmptyBackendEnumValue()) {
            values += StringWithId.emptyBackendEnumValue
        }

        if (currentEnumValue.value == null) {
            // current enum value is not known and thus it cannot exist in localizedEnumValues.
            // It should still be one of the selectable values -> add it
            //
            // - note that we're only adding one unknown value. This respects the requirement
            //   placed by toLocalizedStringWithId function
            values += selectedValue
        }

        values += enumValues.mapNotNull {
            allLocalizedEnumValues[it]
        }

        return StringListField(
                id = fieldId,
                values = values,
                selected = listOf(selectedValue.id),
                configureSettings = configureFieldSettings
        )
    }

    companion object {
        // cannot use reified type parameters in constructor --> separate factory function
        inline fun <reified E> create(
            stringProvider: StringProvider
        ): EnumStringListFieldFactory<E> where E : Enum<E>, E : LocalizableEnum, E : RepresentsBackendEnum {

            val enumValues = enumValues<E>().toList().map {
                it.toBackendEnum()
            }
            return EnumStringListFieldFactory(stringProvider, enumValues) { enumValue, strProvider ->
                enumValue.toLocalizedStringWithId(strProvider)
            }
        }


        // cannot use reified type parameters in constructor --> separate factory function
        inline fun <reified E> createForNonLocalizableEnum(
            stringProvider: StringProvider,
            noinline enumToStringWithIdConverter: (BackendEnum<E>, StringProvider) -> StringWithId
        ): EnumStringListFieldFactory<E> where E : Enum<E>, E : RepresentsBackendEnum {
            val enumValues = enumValues<E>().toList().map {
                it.toBackendEnum()
            }

            return EnumStringListFieldFactory(stringProvider, enumValues, enumToStringWithIdConverter)
        }
    }
}
