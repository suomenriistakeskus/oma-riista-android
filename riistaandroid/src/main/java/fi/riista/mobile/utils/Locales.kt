package fi.riista.mobile.utils

import java.util.Locale

object Locales {

    private const val FINLAND_COUNTRY_CODE = "FI"

    @JvmField
    val FI = Locale(AppPreferences.LANGUAGE_CODE_FI, FINLAND_COUNTRY_CODE)

    @JvmField
    val SV = Locale(AppPreferences.LANGUAGE_CODE_SV, FINLAND_COUNTRY_CODE)

    @JvmField
    val EN = Locale(AppPreferences.LANGUAGE_CODE_EN, FINLAND_COUNTRY_CODE)
}
