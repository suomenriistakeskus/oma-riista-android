package fi.riista.common.network

data class NetworkClientConfiguration(
    /**
     * Are the redirects to absolute hosts allowed?
     *
     * If false, the redirect is only allowed to relative urls i.e. Location header is
     * prefixed with serverBaseAddress.
     */
    val allowRedirectsToAbsoluteHosts: Boolean = false
)
