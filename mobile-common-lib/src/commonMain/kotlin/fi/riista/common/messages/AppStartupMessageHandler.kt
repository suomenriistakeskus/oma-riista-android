package fi.riista.common.messages

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.PlatformName
import fi.riista.common.logging.getLogger
import fi.riista.common.preferences.Preferences

/**
 * A specialized message handler for application startup messages. The main difference
 * to [MessageHandler] is that application startup message is allowed to be displayed
 * only once after app startup.
 */
class AppStartupMessageHandler(
    private val applicationVersion: String,
    private val platformName: PlatformName,
    preferences: Preferences
) {
    internal val delegateMessageHandler by lazy {
        MessageHandler(
                applicationVersion = applicationVersion,
                platformName = platformName,
                preferences = preferences,
                storageKey = "AppStartupMessageDisplayCounts"
        )
    }

    internal val startupMessageDisplayAttempted = AtomicBoolean(false)

    fun parseAppStartupMessageFromJson(messageJson: String?) {
        delegateMessageHandler.parseMessageFromJson(messageJson)
    }

    fun resetStartupMessageDisplayAttempted() {
        startupMessageDisplayAttempted.value = false
    }
    /**
     * Gets the app startup message to be displayed.
     *
     * NOTE: Will automatically increase the message display count if a message is found.
     *
     * Will return the message if
     * - it has been previously set using [parseAppStartupMessageFromJson]
     * - the message display count has not been reached
     * - the message has not yet been displayed during this app startup process
     */
    fun getAppStartupMessageToBeDisplayed(): Message? {
        // assume startup message will be automatically displayed and thus
        // display count should be respected
        return delegateMessageHandler.getMessageIfNotDisplayCountLimited()
            // use ?.takeIf intentionally i.e. don't set display gate unless there's a message
            ?.takeIf { message ->
                val notDisplayedYet = startupMessageDisplayAttempted.compareAndSet(expected = false, new = true)
                if (!notDisplayedYet) {
                    logger.v { "App startup message (id = ${message.id}) has already been displayed" }
                }

                notDisplayedYet
            }?.also { message ->
                // message had not yet been displayed, increase the display count
                delegateMessageHandler.incrementMessageAutomaticDisplayCount(message.id)
            }
    }

    companion object {
        private val logger by getLogger(AppStartupMessageHandler::class)
    }
}