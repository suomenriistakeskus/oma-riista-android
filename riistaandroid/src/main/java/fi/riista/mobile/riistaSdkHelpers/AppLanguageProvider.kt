package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import fi.riista.common.model.Language
import fi.riista.common.resources.LanguageProvider
import fi.riista.mobile.R

class AppLanguageProvider(private val context: Context): LanguageProvider {
    override fun getCurrentLanguage(): Language {
        val languageCode = context.getString(R.string.language_code)
        return Language.fromLanguageCode(languageCode)
                ?: throw RuntimeException("Failed to create a language from languageCode '$languageCode'")
    }
}