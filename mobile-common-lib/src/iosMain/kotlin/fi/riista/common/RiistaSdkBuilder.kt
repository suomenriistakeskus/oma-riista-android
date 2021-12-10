package fi.riista.common

import fi.riista.common.database.DatabaseDriverFactory

actual class RiistaSdkBuilder private constructor(
    internal actual var configuration: RiistaSdkConfiguration,
) {

    /**
     * Initializes the RiistaSDK.
     */
    actual fun initializeRiistaSDK() {
        RiistaSDK.initialize(configuration, DatabaseDriverFactory())
    }

    companion object {
        fun with(
            applicationVersion: String,
            buildVersion: String,
            serverBaseAddress: String,
        ): RiistaSdkBuilder {
            val configuration = RiistaSdkConfiguration(
                    applicationVersion, buildVersion, serverBaseAddress)

            return RiistaSdkBuilder(configuration)
        }
    }
}
