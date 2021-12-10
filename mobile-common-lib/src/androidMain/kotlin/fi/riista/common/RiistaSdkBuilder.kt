package fi.riista.common

import android.content.Context
import fi.riista.common.database.DatabaseDriverFactory
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

        RiistaSDK.initialize(configuration, DatabaseDriverFactory(applicationContext))
    }

    companion object {
        @JvmStatic
        fun with(
            applicationVersion: String,
            buildVersion: String,
            serverBaseAddress: String,
        ): RiistaSdkBuilder {
            val configuration = RiistaSdkConfiguration(
                    applicationVersion, buildVersion, serverBaseAddress)

            return RiistaSdkBuilder(configuration)
        }

        private val logger by getLogger(RiistaSdkBuilder::class)
    }
}
