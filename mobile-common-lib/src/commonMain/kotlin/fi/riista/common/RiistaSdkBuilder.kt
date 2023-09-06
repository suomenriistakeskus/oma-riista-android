package fi.riista.common


expect class RiistaSdkBuilder {
    internal var configuration: RiistaSdkConfiguration

    /**
     * Initializes the RiistaSDK.
     */
    fun initializeRiistaSDK()

    internal fun setupCrashlytics()
}

/**
 * Should the redirects to absolute hosts be allowed?
 *
 * If false, the redirect is only allowed to relative urls i.e. Location header is
 * prefixed with serverBaseAddress.
 */
fun RiistaSdkBuilder.setAllowRedirectsToAbsoluteHosts(allowed: Boolean): RiistaSdkBuilder {
    configuration = configuration.copy(
        networkClientConfiguration = configuration.networkClientConfiguration.copy(
            allowRedirectsToAbsoluteHosts = allowed
        )
    )
    return this
}
