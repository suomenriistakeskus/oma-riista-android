package fi.riista.common.resources

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.dataField.StringListField

internal const val EMPTY_BACKEND_ENUM_VALUE_ID = -1L
internal const val RAW_BACKEND_VALUE_ID = -2L

/**
 * A helper for localizing [Enum] values that can be localized (i.e implement
 * [LocalizableEnum] interface)
 */
internal fun <E> E.toLocalizedStringWithId(
    stringProvider: StringProvider
): StringWithId where E : Enum<E>, E : LocalizableEnum {
    return toLocalizedStringWithId { enumValue ->
        stringProvider.getString(enumValue.resourcesStringId)
    }
}

/**
 * A helper for localizing [Enum] values that don't implement [LocalizableEnum] interface
 */
internal fun <E> E.toLocalizedStringWithId(
    localizationProvider: (E) -> String,
): StringWithId where E : Enum<E> {
    return StringWithId(
        string = localizationProvider(this),
        id = stringId
    )
}

/**
 * A helper for getting stringId for this [Enum] value.
 */
internal val <E> E.stringId: StringId where E : Enum<E>
    get() {
        return ordinal.toLong()
    }

/**
 * A helper for treating [StringWithId.id] as an [Enum] ordinal and getting
 * the matching [Enum] value.
 */
internal inline fun <reified E : Enum<E>> StringWithId.toEnum(): E? {
    val values = enumValues<E>()
    val ordinal = id.toInt()
    return when {
        ordinal >= 0 && ordinal < values.size -> values[ordinal]
        else -> null
    }
}

/**
 * A helper for localizing [BackendEnum] values that can be localized (i.e implement
 * [LocalizableEnum] interface)
 *
 * NOTE: Will use hard coded ids to represent cases where the [BackendEnum.value] is null. The
 * id of the returned [StringWithId] will be:
 *      -1 if [BackendEnum.rawBackendEnumValue] is null
 *      -2 if [BackendEnum.rawBackendEnumValue] is not null
 *
 * This should be taken into account when e.g. storing enum values in list of [StringWithId]s
 * (which is the case e.g. with [StringListField]). In those cases there should be at maximum
 * one [StringWithId] for which the enum value is unknown (value == null, rawBackendEnumValue != null)
 */
internal fun <E> BackendEnum<E>.toLocalizedStringWithId(
    stringProvider: StringProvider
): StringWithId where E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
    return value?.toLocalizedStringWithId(stringProvider)
        ?: toFallbackStringWithId()
}

/**
 * A helper for localizing [BackendEnum] values that don't implement [LocalizableEnum] interface.
 *
 * NOTE: Will use hard coded ids to represent cases where the [BackendEnum.value] is null. The
 * id of the returned [StringWithId] will be:
 *      -1 if [BackendEnum.rawBackendEnumValue] is null
 *      -2 if [BackendEnum.rawBackendEnumValue] is not null
 *
 * This should be taken into account when e.g. storing enum values in list of [StringWithId]s
 * (which is the case e.g. with [StringListField]). In those cases there should be at maximum
 * one [StringWithId] for which the enum value is unknown (value == null, rawBackendEnumValue != null)
 */
internal fun <E> BackendEnum<E>.toLocalizedStringWithId(
    localizationProvider: (E) -> String,
): StringWithId where E : Enum<E>, E : RepresentsBackendEnum {
    return value?.toLocalizedStringWithId(localizationProvider)
        ?: toFallbackStringWithId()
}

private fun <E> BackendEnum<E>.toFallbackStringWithId():
        StringWithId where E : Enum<E>, E : RepresentsBackendEnum {
    return if (!rawBackendEnumValue.isNullOrBlank()) {
        StringWithId(rawBackendEnumValue, RAW_BACKEND_VALUE_ID)
    } else {
        StringWithId.emptyBackendEnumValue
    }
}

/**
 * A helper for treating [StringWithId.id] as an [Enum] (of type [E]) ordinal and getting
 * the matching [BackendEnum] value.
 *
 * Supports unwrapping also unknown values assuming that [StringWithId] was created
 * using [toLocalizedStringWithId].
 */
internal inline fun <reified E> StringWithId.toBackendEnum():
        BackendEnum<E> where E : Enum<E>, E : RepresentsBackendEnum {
    val values = enumValues<E>().toList()
    val enumValue = this.toEnum<E>()

    val ordinal = id
    return when {
        enumValue != null -> BackendEnum(value = enumValue, values)
        ordinal == EMPTY_BACKEND_ENUM_VALUE_ID -> BackendEnum(rawBackendEnumValue = null, values)
        ordinal == RAW_BACKEND_VALUE_ID -> BackendEnum(rawBackendEnumValue = string, values)
        else -> throw AssertionError("Couldn't interpret $id/$string as one of enum $values")
    }
}

/**
 * A helper for determining whether this [StringWithId] represents a [BackendEnum] which doesn't
 * have any value (i.e. [BackendEnum.value] and [BackendEnum.rawBackendEnumValue] are both null).
 */
internal fun StringWithId.isEmptyBackendEnumValue(): Boolean {
    return id == EMPTY_BACKEND_ENUM_VALUE_ID
}

internal val StringWithId.Companion.emptyBackendEnumValue: StringWithId
    get() = StringWithId("", EMPTY_BACKEND_ENUM_VALUE_ID)


/**
 * A helper for localizing [BackendEnum] values that can be localized (i.e implement
 * [LocalizableEnum] interface).
 */
fun <E> BackendEnum<E>.localized(stringProvider: StringProvider): String where
        E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
    return value?.let {
        stringProvider.getString(it.resourcesStringId)
    }
        ?: this.rawBackendEnumValue
        ?: ""
}