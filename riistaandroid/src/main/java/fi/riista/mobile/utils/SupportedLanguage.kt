package fi.riista.mobile.utils

import java.util.Objects.requireNonNull

enum class SupportedLanguage(languageCode: String) {

    FINNISH(AppPreferences.LANGUAGE_CODE_FI),
    SWEDISH(AppPreferences.LANGUAGE_CODE_SV),
    ENGLISH(AppPreferences.LANGUAGE_CODE_EN);

    val languageCode: String = requireNonNull(languageCode)

    fun matchesLanguageCode(languageCode: String?): Boolean = this.languageCode.equals(languageCode, ignoreCase = true)

    companion object {
        @JvmStatic
        fun fromLanguageCode(languageCode: String?): SupportedLanguage? {
            if (languageCode != null) {
                for (lang in values()) {
                    if (lang.matchesLanguageCode(languageCode)) {
                        return lang
                    }
                }
            }

            return null
        }
    }
}
