package fi.riista.common

import android.content.Context
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.logging.CrashlyticsLogger
import fi.riista.common.logging.getLogger

actual class RiistaSdkBuilder private constructor(
    internal actual var configuration: RiistaSdkConfiguration,
) {

    private var applicationContext: Context? = null

    fun registerApplicationContext(applicationContext: Context): RiistaSdkBuilder {
        this.applicationContext = applicationContext
        return this
    }

    /**
     * Initializes the RiistaSDK.
     */
    actual fun initializeRiistaSDK() {
        val applicationContext = this.applicationContext
        if (applicationContext == null) {
            logger.w { "Did you forget to register application context?" }
            throw IllegalArgumentException()
        }
        ApplicationContextHolder.applicationContext = applicationContext

        setupCrashlytics()

        RiistaSDK.initialize(configuration, DatabaseDriverFactory(applicationContext))
    }

    internal actual fun setupCrashlytics() {
        enableCrashlytics()
    }

    companion object {
        @JvmStatic
        fun with(
            applicationVersion: String,
            buildVersion: String,
            serverBaseAddress: String,
            crashlyticsLogger: CrashlyticsLogger,
        ): RiistaSdkBuilder {
            val configuration = RiistaSdkConfiguration(
                applicationVersion = applicationVersion,
                applicationBuild = buildVersion,
                serverBaseAddress = serverBaseAddress,
                crashlyticsLogger = crashlyticsLogger,
            )

            return RiistaSdkBuilder(configuration)
        }

        private val logger by getLogger(RiistaSdkBuilder::class)
    }
}
