package fi.riista.common.domain.model

import fi.riista.common.model.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguageTest {
    @Test
    fun testKnownLanguages() {
        assertEquals(Language.FI, Language.fromLanguageCode("fi"))
        assertEquals(Language.SV, Language.fromLanguageCode("sv"))
        assertEquals(Language.EN, Language.fromLanguageCode("en"))

        assertEquals("fi", Language.FI.languageCode)
        assertEquals("sv", Language.SV.languageCode)
        assertEquals("en", Language.EN.languageCode,)
        assertEquals(3, Language.values().size)
    }

    @Test
    fun testKnownLanguagesWithUpperCaseLetters() {
        assertEquals(Language.FI, Language.fromLanguageCode("Fi"))
        assertEquals(Language.SV, Language.fromLanguageCode("Sv"))
        assertEquals(Language.EN, Language.fromLanguageCode("En"))

        assertEquals(Language.FI, Language.fromLanguageCode("FI"))
        assertEquals(Language.SV, Language.fromLanguageCode("SV"))
        assertEquals(Language.EN, Language.fromLanguageCode("EN"))

        assertEquals(Language.FI, Language.fromLanguageCode("fI"))
        assertEquals(Language.SV, Language.fromLanguageCode("sV"))
        assertEquals(Language.EN, Language.fromLanguageCode("eN"))
    }

    @Test
    fun testUnknownLanguages() {
        assertNull(Language.fromLanguageCode("de"))
        assertNull(Language.fromLanguageCode("ru"))
        assertNull(Language.fromLanguageCode("fr"))
    }

    @Test
    fun testLanguagesWithLocale() {
        assertNull(Language.fromLanguageCode("en-US"))
        assertNull(Language.fromLanguageCode("fi-FI"))
        assertNull(Language.fromLanguageCode("sv-SV"))
    }
}
