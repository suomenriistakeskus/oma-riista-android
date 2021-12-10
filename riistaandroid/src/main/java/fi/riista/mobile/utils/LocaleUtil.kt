package fi.riista.mobile.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_FI
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_SV
import java.util.Locale

object LocaleUtil {

    @JvmStatic
    fun setupLocale(context: Context): Context {
        val locale = localeFromLanguageSetting(context)
        Locale.setDefault(locale)

        //
        // TODO For time being, this if block is commented out since it does not seem to work right.
        //
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //    return updateResources(context, locale)
        //}

        return updateResourcesLegacy(context, locale)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources: Resources = context.resources

        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }

    /**
     * @return Locale from language code setting with associated country code
     */
    @JvmStatic
    fun localeFromLanguageSetting(context: Context): Locale {
        val languageCode = AppPreferences.getLanguageCodeSetting(context)

        return localeFromLanguage(languageCode)
    }

    /**
     * @param languageCode Two letter language code
     * @return Locale from language code with associated country code
     */
    @JvmStatic
    fun localeFromLanguage(languageCode: String): Locale {
        return when (languageCode) {
            LANGUAGE_CODE_FI -> Locales.FI
            LANGUAGE_CODE_SV -> Locales.SV
            else -> Locales.EN
        }
    }

    @JvmStatic
    fun getLocalizedValue(values: HashMap<String, String>?): String {
        var value: String? = null

        if (values != null) {
            // TODO Should AppPreferences.getLanguageCodeSetting be used instead?
            val activeLanguage = Locale.getDefault().language
            value = values[activeLanguage]

            if (value == null) {
                value = values[LANGUAGE_CODE_FI] // Fallback to Finnish
            }
        }

        return value ?: ""
    }
}
