package fi.riista.common.domain.userInfo

interface UsernameProvider {
    /**
     * Username of the logged in user, or if login has not yet been performed then fallback/cached
     * username (if available).
     */
    val username: String?
}
