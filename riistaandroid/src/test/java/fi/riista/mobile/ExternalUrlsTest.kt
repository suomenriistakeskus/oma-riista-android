package fi.riista.mobile

import fi.riista.mobile.ExternalUrls.Companion.getEventSearchUrl
import fi.riista.mobile.ExternalUrls.Companion.getHunterMagazineUrl
import fi.riista.mobile.ExternalUrls.Companion.getHuntingSeasonsUrl
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_EN
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_FI
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_SV
import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalUrlsTest {

    @Test
    fun testGetHuntingSeasonsUrl() {
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_FI, getHuntingSeasonsUrl(LANGUAGE_CODE_FI))
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_SV, getHuntingSeasonsUrl(LANGUAGE_CODE_SV))
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_EN, getHuntingSeasonsUrl(LANGUAGE_CODE_EN))

        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_FI, getHuntingSeasonsUrl(null))
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_FI, getHuntingSeasonsUrl(""))
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_FI, getHuntingSeasonsUrl(UNSUPPORTED_LANGUAGE_CODE))
        assertEquals(ExternalUrls.HUNTING_SEASONS_URL_FI, getHuntingSeasonsUrl(NONEXISTENT_LANGUAGE_CODE))
    }

    @Test
    fun testGetEventSearchUrl() {
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(LANGUAGE_CODE_FI))
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_SV, getEventSearchUrl(LANGUAGE_CODE_SV))
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(LANGUAGE_CODE_EN))

        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(null))
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(""))
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(UNSUPPORTED_LANGUAGE_CODE))
        assertEquals(ExternalUrls.EVENT_SEARCH_URL_FI, getEventSearchUrl(NONEXISTENT_LANGUAGE_CODE))
    }

    @Test
    fun testGetHunterMagazineUrl() {
        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(LANGUAGE_CODE_FI))
        assertEquals(ExternalUrls.MAGAZINE_URL_SV, getHunterMagazineUrl(LANGUAGE_CODE_SV))
        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(LANGUAGE_CODE_EN))

        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(null))
        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(""))
        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(UNSUPPORTED_LANGUAGE_CODE))
        assertEquals(ExternalUrls.MAGAZINE_URL_FI, getHunterMagazineUrl(NONEXISTENT_LANGUAGE_CODE))
    }

    companion object {
        private const val UNSUPPORTED_LANGUAGE_CODE = "es"
        private const val NONEXISTENT_LANGUAGE_CODE = "xyzzy"
    }
}
