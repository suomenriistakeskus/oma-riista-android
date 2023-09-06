package fi.riista.common

data class VersionInfo(
    // e.g. 2.4.4
    val appVersion: String,
    // e.g. 2.4.4.2 (on iOS) or 1197 (on android)
    val appBuild: String,
) {
    override fun toString(): String {
        return "VersionInfo(appVersion='$appVersion', appBuild='$appBuild')"
    }
}