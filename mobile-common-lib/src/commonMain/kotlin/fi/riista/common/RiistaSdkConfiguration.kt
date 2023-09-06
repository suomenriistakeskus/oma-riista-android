package fi.riista.common

import fi.riista.common.logging.CrashlyticsLogger
import fi.riista.common.network.NetworkClientConfiguration

data class RiistaSdkConfiguration(
    val versionInfo: VersionInfo,
    // The server base address without trailing slash ('/') e.g. "https://oma.riista.fi"
    val serverBaseAddress: String,
    val platform: Platform = Platform(),
    val device: Device = Device(),
    val networkClientConfiguration: NetworkClientConfiguration = NetworkClientConfiguration(),
    val crashlyticsLogger: CrashlyticsLogger,
) {
    init {
        if (serverBaseAddress.last() == '/') {
            throw AssertionError("Error: server base address must not contain trailing slash ('/')")
        }
    }

    val userAgent: String = "Oma Riista/${versionInfo.appVersion}"

    internal constructor(applicationVersion: String, applicationBuild: String,
                         serverBaseAddress: String, crashlyticsLogger: CrashlyticsLogger)
            : this(versionInfo = VersionInfo(applicationVersion, applicationBuild),
                   serverBaseAddress = serverBaseAddress, crashlyticsLogger = crashlyticsLogger)
}
