package fi.riista.common.network.cookies

/**
 * a data class for exposing Cookie data (outside of RiistaSDK). We don't want
 * to expose internal implementation (i.e. ktor classes)
 */
data class CookieData(
    val domain: String?,
    // milliseconds since epoch
    val expiresTimestamp: Long?,
    val httpOnly: Boolean,
    val name: String,
    val path: String?,
    val secure: Boolean,
    val value: String
)
