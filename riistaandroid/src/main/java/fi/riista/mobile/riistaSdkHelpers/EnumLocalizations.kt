package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum

fun <E> BackendEnum<E>.localized(context: Context): String where
        E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
    return value?.let {
        val stringProvider = ContextStringProviderFactory.createForContext(context)
        stringProvider.getString(it.resourcesStringId)
    }
        ?: this.rawBackendEnumValue
        ?: ""
}
