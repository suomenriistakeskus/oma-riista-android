package fi.riista.common

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.logging.CrashlyticsLogger

actual class RiistaSdkBuilder private constructor(
    internal actual var configuration: RiistaSdkConfiguration,
) {

    /**
     * Initializes the RiistaSDK.
     */
    actual fun initializeRiistaSDK() {
        setupCrashlytics()
        RiistaSDK.initialize(configuration, DatabaseDriverFactory())
    }

    internal actual fun setupCrashlytics() {
        enableCrashlytics()
        setCrashlyticsUnhandledExceptionHook()
    }

    companion object {
        fun with(
            applicationVersion: String,
            buildVersion: String,
            serverBaseAddress: String,
            crashlyticsLogger: CrashlyticsLogger,
        ): RiistaSdkBuilder {
            val configuration = RiistaSdkConfiguration(
                    applicationVersion, buildVersion, serverBaseAddress, crashlyticsLogger)

            return RiistaSdkBuilder(configuration)
        }
    }
}
