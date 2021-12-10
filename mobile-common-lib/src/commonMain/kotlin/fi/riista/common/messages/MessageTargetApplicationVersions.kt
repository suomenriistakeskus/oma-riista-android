package fi.riista.common.messages

import fi.riista.common.PlatformName
import kotlinx.serialization.Serializable

@Serializable
data class MessageTargetApplicationVersions(
    // the application versions allowed to display the message e.g. ["2.4.3", "2.4.4"]
    // Not taken into account if null.
    val ios: List<String>?,

    // the application versions allowed to display the message e.g. ["2.4.5"]
    // Not taken into account if null.
    val android: List<String>?
) {
    fun targetVersionsForPlatform(platformName: PlatformName): List<String>? {
        return when (platformName) {
            PlatformName.IOS -> ios
            PlatformName.ANDROID -> android
        }
    }

    override fun toString(): String {
        return "MessageTargetApplicationVersions(ios=$ios, android=$android)"
    }
}
