package fi.riista.common.messages

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.PlatformName
import fi.riista.common.domain.dto.MessageDTO
import fi.riista.common.domain.dto.toMessage
import fi.riista.common.logging.getLogger
import fi.riista.common.messages.Message.Companion.DISPLAY_COUNT_NOT_LIMITED
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.deserializeFromJson

class MessageHandler(
    private val applicationVersion: String,
    private val platformName: PlatformName,

    // the key to be used when saving display counts to persistent storage
    private val storageKey: String,
    preferences: Preferences
) {
    private val displayCountTracker by lazy {
        MessageDisplayCountTracker(
                preferences = preferences,
                maxDisplayCountsToTrack = 5,
                storageKey = storageKey
        )
    }

    private val message = AtomicReference<Message?>(null)

    fun parseMessageFromJson(messageJson: String?) {
        messageJson
            ?.deserializeFromJson<MessageDTO>()
            ?.toMessage()
            ?.also { msg ->
                logger.i { "Parsed a message (id = ${msg.id})" }
                message.set(msg)
            }
    }

    /**
     * Gets the message.
     *
     * Note: does NOT check message display count limits. Call [getMessageIfNotDisplayCountLimited]
     * for that purpose.
     *
     * Will return the message if
     * - it has been previously set using [parseMessageFromJson]
     * - it is targeted for the [platformName] and [applicationVersion]
     */
    fun getMessage(): Message? {
        return message.get()
            ?.takeIf { message ->
                val targetedForAppVersion: Boolean = message.targetApplicationVersions
                    ?.targetVersionsForPlatform(platformName)
                    ?.contains(applicationVersion) ?: true

                targetedForAppVersion.also {
                    if (it) {
                        logger.v { "Message (id: ${message.id}) targeted for $applicationVersion." }
                    } else {
                        logger.v { "Message (id: ${message.id}) not targeted for $applicationVersion." +
                                " (target versions: ${message.targetApplicationVersions})" }
                    }
                }
            }
    }

    /**
     * Same as [getMessage] but checks also the message display count. Only returns
     * the message if the message display count has not been reached.
     */
    fun getMessageIfNotDisplayCountLimited(): Message? {
        return getMessage()?.takeIf { message ->
            val messageId = message.id

            if (message.maxDisplayCount == DISPLAY_COUNT_NOT_LIMITED ||
                displayCountTracker.displayCountFor(messageId) < message.maxDisplayCount) {
                true
            } else {
                logger.v { "Refusing to display message $messageId " +
                        "(display count limit ${message.maxDisplayCount} reached)" }
                false
            }
        }
    }

    /**
     * Gets how many times the specified message has been displayed automatically.
     *
     * Automatically i.e. if there's a button in the UI for viewing the message then that
     * display should probably not affect message display count.
     */
    fun getMessageAutomaticDisplayCount(messageId: MessageId): Int {
        return displayCountTracker.displayCountFor(messageId)
    }

    /**
     * Increments the number how many times the specified message has been displayed
     * automatically (i.e. without user requesting).
     */
    fun incrementMessageAutomaticDisplayCount(messageId: MessageId) {
        displayCountTracker.incrementDisplayCountFor(messageId)
    }

    companion object {
        private val logger by getLogger(MessageHandler::class)
    }
}