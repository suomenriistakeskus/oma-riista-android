package fi.riista.common.resources

import fi.riista.common.model.Language

interface LanguageProvider {
    fun getCurrentLanguage(): Language
}