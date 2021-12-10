package fi.riista.common.model

import kotlin.jvm.JvmStatic

enum class Language(val languageCode: String) {
    FI("fi"),
    SV("sv"),
    EN("en"),
    ;

    companion object {
        @JvmStatic
        fun fromLanguageCode(languageCode: String?): Language? {
            return languageCode?.lowercase()?.let { language ->
                values().firstOrNull { it.languageCode == language }
            }
        }
    }
}
