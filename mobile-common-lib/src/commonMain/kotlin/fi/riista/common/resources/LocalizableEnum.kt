package fi.riista.common.resources

/**
 * An interface for [Enum]s that can be localized.
 */
interface LocalizableEnum {
    val resourcesStringId: RR.string
}

fun <E> E.localized(stringProvider: StringProvider): String where E : Enum<E>, E : LocalizableEnum {
    return stringProvider.getString(resourcesStringId)
}
