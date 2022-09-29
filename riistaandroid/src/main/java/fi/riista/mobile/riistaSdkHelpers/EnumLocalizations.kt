package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.localized

fun <E> BackendEnum<E>.localized(context: Context): String where
        E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
    val stringProvider = ContextStringProviderFactory.createForContext(context)
    return localized(stringProvider)
}
