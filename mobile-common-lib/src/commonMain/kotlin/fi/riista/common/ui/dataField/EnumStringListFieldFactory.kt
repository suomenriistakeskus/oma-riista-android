package fi.riista.common.ui.dataField

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.*
import fi.riista.common.resources.isEmptyBackendEnumValue
import fi.riista.common.resources.toLocalizedStringWithId

/**
 * A factory that is able to create [StringListField]s based on given [BackendEnum]
 * (that wraps enums of type [E])
 */
internal class EnumStringListFieldFactory<E>(
    private val stringProvider: StringProvider,
    enumValues: List<E>
) where E : Enum<E>, E : LocalizableEnum, E : RepresentsBackendEnum {

    private val localizedEnumValues: List<StringWithId> by lazy {
        enumValues.map {
            it.toLocalizedStringWithId(stringProvider)
        }
    }

    fun <DataId: DataFieldId> create(
        fieldId: DataId,
        currentEnumValue: BackendEnum<E>,
        allowEmptyValue: Boolean,
        configureFieldSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null,
    ): StringListField<DataId> {
        val selectedValue = currentEnumValue.toLocalizedStringWithId(stringProvider)
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

        values += localizedEnumValues

        return StringListField(
                id = fieldId,
                values = values,
                selected = selectedValue.id,
                configureSettings = configureFieldSettings
        )
    }

    companion object {
        // cannot use reified type parameters in constructor --> separate factory function
        inline fun <reified E> create(
            stringProvider: StringProvider
        ): EnumStringListFieldFactory<E> where E : Enum<E>, E : LocalizableEnum, E : RepresentsBackendEnum {

            val enumValues = enumValues<E>().toList()
            return EnumStringListFieldFactory(stringProvider, enumValues)
        }
    }
}