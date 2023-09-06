package fi.riista.common.network.cookies

import fi.riista.common.logging.getLogger
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.hostIsIp
import io.ktor.http.isSecure
import io.ktor.util.date.GMTDate
import kotlinx.datetime.Clock
import kotlin.collections.List
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toList

class CustomCookiesStorage: CookiesStorage {
    // cannot use normal MutableList here since cookies get added from background thread
    // which would cause InvalidMutabilityException on iOS.
    private val _addedCookies = mutableMapOf<String, CookieData>()
    val addedCookies: List<CookieData>
        get() = _addedCookies.values.toList()

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
            val cookieData = it.toCookieData()
            // replace existing cookie with same name (e.g. session cookie stays valid)
            _addedCookies[cookieData.name] = cookieData
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

    fun getCookies(requestUrl: String): List<CookieData> {
        val url = Url(requestUrl)
        return addedCookies.filter { it.matches(url) }
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

// Ktor has similar functionality for Cookie but unfortunately the necessary function
// is internal and thus inaccessible for us. Copy the relevant code in order to filter
// only matching cookies

private fun CookieData.matches(requestUrl: Url): Boolean {
    val domain = domain?.toLowerCasePreservingASCIIRules()?.trimStart('.')
        ?: error("Domain field should have the default value")

    val path = with(path) {
        val current = path ?: error("Path field should have the default value")
        if (current.endsWith('/')) current else "$path/"
    }

    val host = requestUrl.host.toLowerCasePreservingASCIIRules()
    val requestPath = let {
        val pathInRequest = requestUrl.encodedPath
        if (pathInRequest.endsWith('/')) pathInRequest else "$pathInRequest/"
    }

    if (host != domain && (hostIsIp(host) || !host.endsWith(".$domain"))) {
        return false
    }

    if (path != "/" &&
        requestPath != path &&
        !requestPath.startsWith(path)
    ) return false

    return !(secure && !requestUrl.protocol.isSecure())
}

private fun String.toLowerCasePreservingASCIIRules(): String {
    val firstIndex = indexOfFirst {
        toLowerCasePreservingASCII(it) != it
    }

    if (firstIndex == -1) {
        return this
    }

    val original = this
    return buildString(length) {
        append(original, 0, firstIndex)

        for (index in firstIndex..original.lastIndex) {
            append(toLowerCasePreservingASCII(original[index]))
        }
    }
}

private fun toLowerCasePreservingASCII(ch: Char): Char = when (ch) {
    in 'A'..'Z' -> ch + 32
    in '\u0000'..'\u007f' -> ch
    else -> ch.lowercaseChar()
}
