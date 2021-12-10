package fi.riista.common.network.cookies

import co.touchlab.stately.collections.IsoMutableList
import fi.riista.common.logging.getLogger
import io.ktor.client.features.cookies.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.datetime.Clock

class CustomCookiesStorage: CookiesStorage {
    // cannot use normal MutableList here since cookies get added from background thread
    // which would cause InvalidMutabilityException on iOS.
    private val _addedCookies = IsoMutableList<CookieData>()
    val addedCookies: List<CookieData> = _addedCookies

    private val delegateStorage = AcceptAllCookiesStorage()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        // enable when testing
        //logger.v { "Received cookie for $requestUrl, cookie = $cookie\n" }

        val fixedCookie = fixCookieForIOS10(cookie)

        // enable when testing
        //logger.v { "\nAdding cookie for $requestUrl, cookie = $fixedCookie\n\n" }

        delegateStorage.addCookie(requestUrl, fixedCookie)

        // the stored cookie is different than what we just got (defaults are filled internally)
        // -> keep track of modified cookies instead of ones given to us
        delegateStorage.get(requestUrl).forEach {
            _addedCookies.add(it.toCookieData())
        }
    }

    /**
     * Fixes the given [cookie] for IOS10 devices (or other 32bit devices) that
     * may have incorrectly determined the expires date.
     *
     * The issue in Ktor is that it maximum GMTDate timestamp is Int.MAX_VALUE seconds from
     * epoch. For 32bit devices that receive a cookie with Max-Age == Int.MAX_VALUE, timestamp
     * exceeds the maximum value.
     *
     * The ktor commit that probably fixes the issue:
     * https://github.com/ktorio/ktor/pull/2685/commits/c4e871652e3c802ca62883167be9af620f6f51e9
     * but that has not yet been released.
     */
    private fun fixCookieForIOS10(cookie: Cookie): Cookie {
        val nowMillis = Clock.System.now().toEpochMilliseconds()

        // The delegateStorage works based on cookie.expires field and more specifically
        // cookie.expires.timestamp. Require valid timestamp
        val needsFix = cookie.expires?.timestamp?.let { timestamp ->
            // timestamp is milliseconds from epoch. Require valid timestamp if
            // cookie Max-Age is defined
            val timestampNowOrInPast = timestamp <= nowMillis

            cookie.maxAge > 0 && timestampNowOrInPast
        } ?: false

        if (needsFix) {
            // due to ktor issue the GMTDate on 32bit iOS devices (iOS10) can
            // only be Int.MAX_VALUE seconds from epoch.
            val timestamp = (cookie.maxAge * 1000L + nowMillis)
                .coerceAtMost(Int.MAX_VALUE * 1000L)

            return cookie.copy(
                expires = GMTDate(timestamp)
            )/*.also {
                // enable when testing
                logger.v { "Fixing cookie $cookie to $it" }
            }*/
        }
        return cookie
    }

    override fun close() {
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val cookie = delegateStorage.get(requestUrl)
        // enable when testing
        // logger.v { "Returning cookie for $requestUrl, cookie = $cookie" }
        return cookie
    }

    companion object {
        private val logger by getLogger(CustomCookiesStorage::class)
    }
}

private fun Cookie.toCookieData(): CookieData {
    return CookieData(
        domain = domain,
        expiresTimestamp = expires?.timestamp,
        httpOnly = httpOnly,
        name = name,
        path = path,
        secure = secure,
        value = value
    )
}