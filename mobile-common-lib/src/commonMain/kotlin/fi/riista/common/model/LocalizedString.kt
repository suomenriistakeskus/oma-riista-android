package fi.riista.common.model

import fi.riista.common.resources.LanguageProvider
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedString(
    val fi: String?,
    val sv: String?,
    val en: String?,
) {
    fun localized(languageCode: String): String? {
        return Language.fromLanguageCode(languageCode)?.let {
            localized(it)
        }
    }

    fun localized(language: Language): String? {
        // don't use any fallbacks as callee may wish to not use value at all
        // if it doesn't exist for the preferred language.
        return when (language) {
            Language.FI -> fi
            Language.SV -> sv
            Language.EN -> en
        }
    }
}

fun LocalizedString.localizedWithFallbacks(languageProvider: LanguageProvider): String? {
    return this.localized(languageProvider.getCurrentLanguage())
        ?: this.localized(Language.FI)
        ?: this.localized(Language.EN)
        ?: this.localized(Language.SV)
}

