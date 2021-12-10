package fi.riista.common

import fi.riista.common.network.NetworkClientConfiguration

data class RiistaSdkConfiguration(
    val versionInfo: VersionInfo,
    // The server base address without trailing slash ('/') e.g. "https://oma.riista.fi"
    val serverBaseAddress: String,
    val platform: Platform = Platform(),
    val device: Device = Device(),
    val networkClientConfiguration: NetworkClientConfiguration = NetworkClientConfiguration()
) {
    init {
        if (serverBaseAddress.last() == '/') {
            throw AssertionError("Error: server base address must not contain trailing slash ('/')")
        }
    }

    val userAgent: String = "Oma Riista/${versionInfo.appVersion}"

    internal constructor(applicationVersion: String, applicationBuild: String,
                         serverBaseAddress: String)
            : this(versionInfo = VersionInfo(applicationVersion, applicationBuild, RiistaSDK.SDK_VERSION),
                   serverBaseAddress = serverBaseAddress)
}
