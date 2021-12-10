package fi.riista.common.model

import kotlinx.serialization.Serializable

/**
 * An interface for the [Enum]s that can be mapped to enums on the backend.
 *
 * The string value of the backend enum value is to be held in [rawBackendEnumValue].
 */
interface RepresentsBackendEnum {
    val rawBackendEnumValue: String
}

/**
 * Wraps the raw enum value provided by the backend and provides a way for trying
 * to interpret it as an [Enum] value of type [EnumType].
 *
 * The reason behind keeping the backend raw enum value is that client app can safely handle
 * the situation where new enum members get added to the enum in the backend. The client
 * will be able to interpret known values and unknown values can still be kept untouched
 * using [rawBackendEnumValue].
 *
 * This allows modifying other object data without altering enum data (e.g. changing hunting
 * day date without altering the hunting method)
 */
@Serializable
data class BackendEnum<EnumType>(
    /**
     * The raw/string value of the enum value on the backend. Allowed to be null
     * in case DTO value is null.
     */
    val rawBackendEnumValue: String?,
    private val enumValues: List<EnumType>,
) where EnumType : Enum<EnumType>, EnumType : RepresentsBackendEnum {

    constructor(value: EnumType?, values: List<EnumType>):
            this(value?.rawBackendEnumValue, values)

    val value: EnumType? by lazy {
        enumValues.firstOrNull { it.rawBackendEnumValue == rawBackendEnumValue }
    }

    fun create(newValue: EnumType): BackendEnum<EnumType> {
        return BackendEnum(newValue.rawBackendEnumValue, enumValues)
    }

    companion object {
        // cannot have reified EnumType is constructor. Use a separate function instead.
        inline fun <reified EnumType> create(value: EnumType?): BackendEnum<EnumType> where
                EnumType : Enum<EnumType>, EnumType : RepresentsBackendEnum {

            return BackendEnum(value, enumValues<EnumType>().toList())
        }
    }
}

/**
 * Wrap the string to a [BackendEnum] in order to allow safe conversion to an
 * [EnumType] value.
 */
inline fun <reified EnumType> String?.toBackendEnum():
        BackendEnum<EnumType> where EnumType : Enum<EnumType>, EnumType : RepresentsBackendEnum {
    return BackendEnum(this, enumValues<EnumType>().toList())
}

inline fun <reified EnumType> EnumType.toBackendEnum():
        BackendEnum<EnumType> where EnumType : Enum<EnumType>, EnumType : RepresentsBackendEnum {
    return BackendEnum.create(this)
}