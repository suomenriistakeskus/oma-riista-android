package fi.riista.mobile.utils

import fi.riista.mobile.utils.SupportedLanguage.ENGLISH
import fi.riista.mobile.utils.SupportedLanguage.FINNISH
import fi.riista.mobile.utils.SupportedLanguage.SWEDISH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SupportedLanguageTest {

    @Test
    fun testGetLanguageCode() {
        assertEquals("fi", FINNISH.languageCode)
        assertEquals("sv", SWEDISH.languageCode)
        assertEquals("en", ENGLISH.languageCode)
    }

    @Test
    fun testMatchesLanguageCode() {
        assertTrue(FINNISH.matchesLanguageCode("fi"))
        assertTrue(FINNISH.matchesLanguageCode("FI"))
        assertTrue(FINNISH.matchesLanguageCode("fI"))
        assertTrue(FINNISH.matchesLanguageCode("Fi"))

        assertTrue(SWEDISH.matchesLanguageCode("sv"))
        assertTrue(SWEDISH.matchesLanguageCode("SV"))
        assertTrue(SWEDISH.matchesLanguageCode("sV"))
        assertTrue(SWEDISH.matchesLanguageCode("Sv"))

        assertTrue(ENGLISH.matchesLanguageCode("en"))
        assertTrue(ENGLISH.matchesLanguageCode("EN"))
        assertTrue(ENGLISH.matchesLanguageCode("eN"))
        assertTrue(ENGLISH.matchesLanguageCode("En"))

        for (languageCode in getUnsupportedLanguageCodes()) {
            assertFalse(FINNISH.matchesLanguageCode(languageCode))
            assertFalse(SWEDISH.matchesLanguageCode(languageCode))
            assertFalse(ENGLISH.matchesLanguageCode(languageCode))
        }
    }

    @Test
    fun testFromLanguageCode() {
        assertEquals(FINNISH, SupportedLanguage.fromLanguageCode("fi"))
        assertEquals(FINNISH, SupportedLanguage.fromLanguageCode("FI"))
        assertEquals(SWEDISH, SupportedLanguage.fromLanguageCode("sv"))
        assertEquals(SWEDISH, SupportedLanguage.fromLanguageCode("SV"))
        assertEquals(ENGLISH, SupportedLanguage.fromLanguageCode("en"))
        assertEquals(ENGLISH, SupportedLanguage.fromLanguageCode("EN"))

        assertNull(SupportedLanguage.fromLanguageCode(null))
        assertNull(SupportedLanguage.fromLanguageCode(""))
        assertNull(SupportedLanguage.fromLanguageCode(" "))

        for (languageCode in getUnsupportedLanguageCodes()) {
            assertNull(SupportedLanguage.fromLanguageCode(languageCode))
        }
    }

    private fun getUnsupportedLanguageCodes(): List<String> {
        val unsupportedLanguageCodes = mutableListOf<String>()

        for (locale in Locale.getAvailableLocales()) {
            val lang = locale.language
            val notSupported = !FINNISH.matchesLanguageCode(lang)
                    && !SWEDISH.matchesLanguageCode(lang)
                    && !ENGLISH.matchesLanguageCode(lang)

            if (notSupported) {
                unsupportedLanguageCodes.add(lang)
            }
        }

        // Test invariant
        assertFalse(unsupportedLanguageCodes.isEmpty())

        return unsupportedLanguageCodes
    }
}
