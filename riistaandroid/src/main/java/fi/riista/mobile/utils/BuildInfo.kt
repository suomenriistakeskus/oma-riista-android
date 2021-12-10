package fi.riista.mobile.utils

import fi.riista.mobile.BuildConfig

object BuildInfo {
    @JvmStatic
    fun isTestBuild(): Boolean {
        return BuildConfig.FLAVOR in arrayOf("dev", "staging")
    }
}
