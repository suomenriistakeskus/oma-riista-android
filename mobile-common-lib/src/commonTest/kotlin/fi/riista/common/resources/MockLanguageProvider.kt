package fi.riista.common.resources

import fi.riista.common.model.Language

class MockLanguageProvider(
    private val language: Language = Language.FI
): LanguageProvider {
    override fun getCurrentLanguage(): Language = language
}
