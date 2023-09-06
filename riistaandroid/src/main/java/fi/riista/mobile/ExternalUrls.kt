package fi.riista.mobile

import fi.riista.mobile.utils.SupportedLanguage
import fi.riista.mobile.utils.SupportedLanguage.*

class ExternalUrls(selectedLanguage: SupportedLanguage?) {

    // Defaults to using Finnish language if parameter is null.
    private val selectedLanguage: SupportedLanguage = selectedLanguage ?: FINNISH

    // Defaults to using Finnish language if supported language cannot be determined from the given parameter.
    constructor(selectedLanguageCode: String?) : this(SupportedLanguage.fromLanguageCode(selectedLanguageCode))

    val huntingSeasonsUrl: String
        get() = when (selectedLanguage) {
            FINNISH -> HUNTING_SEASONS_URL_FI
            SWEDISH -> HUNTING_SEASONS_URL_SV
            ENGLISH -> HUNTING_SEASONS_URL_EN
        }

    // Separate English URL not available
    val eventSearchUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> EVENT_SEARCH_URL_FI
            SWEDISH -> EVENT_SEARCH_URL_SV
        }

    // Separate English URL not available
    val hunterMagazineUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> MAGAZINE_URL_FI
            SWEDISH -> MAGAZINE_URL_SV
        }

    // Separate English URL not available
    val privacyStatementUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> PRIVACY_STATEMENT_URL_FI
            SWEDISH -> PRIVACY_STATEMENT_URL_SV
        }

    // Separate English and Swedish URLs not available
    val accessibilityUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> ACCESSIBILITY_URL_FI
            SWEDISH -> ACCESSIBILITY_URL_SV
        }

    // Separate English URL not available
    val termsOfServiceUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> TERMS_OF_SERVICE_URL_FI
            SWEDISH -> TERMS_OF_SERVICE_URL_SV
        }

    // Separate English URL not available
    val insuranceInstructionsUrl: String
        get() = when (selectedLanguage) {
            FINNISH, ENGLISH -> INSURANCE_INSTRUCTIONS_URL_FI
            SWEDISH -> INSURANCE_INSTRUCTIONS_URL_SV
        }

    companion object {

        internal const val MAGAZINE_URL_FI = "https://www.metsastajalehti.fi"
        internal const val MAGAZINE_URL_SV = "https://www.jagarentidningen.fi"

        internal const val HUNTING_SEASONS_URL_FI = "https://riista.fi/metsastys/metsastysajat"
        internal const val HUNTING_SEASONS_URL_SV = "https://riista.fi/sv/jakt/jakttider"
        internal const val HUNTING_SEASONS_URL_EN = "https://riista.fi/en/hunting/open-seasons"

        internal const val EVENT_SEARCH_URL_FI = "https://riista.fi/metsastys/tapahtumahaku/"
        internal const val EVENT_SEARCH_URL_SV = "https://riista.fi/sv/jakt/sok-evenemang/"

        internal const val PRIVACY_STATEMENT_URL_FI = "https://riista.fi/riistahallinto/sahkoinen-asiointi/oma-riista-rekisteriseloste/"
        internal const val PRIVACY_STATEMENT_URL_SV = "https://riista.fi/sv/viltforvaltningen/elektroniska-tjanster/registerbeskrivningen/"

        internal const val ACCESSIBILITY_URL_FI = "https://riista.fi/riistahallinto/sahkoinen-asiointi/saavutettavuusselosteet/"
        internal const val ACCESSIBILITY_URL_SV = "https://riista.fi/sv/viltforvaltningen/elektroniska-tjanster/tillganglighetsutlatande/"

        internal const val TERMS_OF_SERVICE_URL_FI = "https://riista.fi/riistahallinto/sahkoinen-asiointi/oma-riista-kayttoehdot/"
        internal const val TERMS_OF_SERVICE_URL_SV = "https://riista.fi/sv/viltforvaltningen/elektroniska-tjanster/anvandarvillkor/"

        internal const val INSURANCE_INSTRUCTIONS_URL_FI = "https://riista.fi/metsastys/palvelut-metsastajalle/vakuutukset/metsastajavakuutus/"
        internal const val INSURANCE_INSTRUCTIONS_URL_SV = "https://riista.fi/sv/jakt/tjanster-for-jagarna/forsakringar/jagarforsakring/"

        @JvmStatic
        fun getHuntingSeasonsUrl(languageCode: String?): String = ExternalUrls(languageCode).huntingSeasonsUrl

        @JvmStatic
        fun getEventSearchUrl(languageCode: String?): String = ExternalUrls(languageCode).eventSearchUrl

        @JvmStatic
        fun getHunterMagazineUrl(languageCode: String?): String = ExternalUrls(languageCode).hunterMagazineUrl

        @JvmStatic
        fun getPrivacyStatementUrl(languageCode: String?): String = ExternalUrls(languageCode).privacyStatementUrl

        @JvmStatic
        fun getAccessibilityUrl(languageCode: String): String = ExternalUrls(languageCode).accessibilityUrl

        @JvmStatic
        fun getTermsOfServiceUrl(languageCode: String?): String = ExternalUrls(languageCode).termsOfServiceUrl

        @JvmStatic
        fun getInsuranceInstructionsUrl(languageCode: String?): String = ExternalUrls(languageCode).insuranceInstructionsUrl
    }
}
