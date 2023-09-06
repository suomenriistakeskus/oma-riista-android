package fi.riista.common.messages

import fi.riista.common.preferences.Preferences
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.removeFirst
import fi.riista.common.util.serializeToJson
import kotlinx.serialization.Serializable

@Serializable
internal data class MessageDisplayCount(
    val messageId: MessageId,
    var displayCount: Int
)

class MessageDisplayCountTracker internal constructor(
    private val preferences: Preferences,

    // how many display counts are tracked? By setting a reasonable limit
    // the tracker will eventually forget the earliest messages
    private val maxDisplayCountsToTrack: Int,

    // the key to be used when saving display counts to persistent storage
    private val storageKey: String,
) {
    internal val messageDisplayCounts = mutableListOf<MessageDisplayCount>()

    init {
        loadDisplayCounts()
    }

    fun incrementDisplayCountFor(messageId: MessageId) {
        val displayCount = displayCountFor(messageId)
        setDisplayCountFor(messageId, displayCount + 1)
    }

    fun displayCountFor(messageId: MessageId): Int {
        return messageDisplayCounts
            .firstOrNull { it.messageId == messageId }
            ?.displayCount ?: 0
    }

    fun setDisplayCountFor(messageId: MessageId, displayCount: Int) {
        messageDisplayCounts.removeFirst { it.messageId == messageId }
        messageDisplayCounts.add(MessageDisplayCount(messageId, displayCount))

        trimDisplayCountsToLimit()
        saveDisplayCounts()
    }

    private fun trimDisplayCountsToLimit() {
        if (messageDisplayCounts.size > maxDisplayCountsToTrack) {
            messageDisplayCounts.removeAt(0)
        }
    }

    private fun loadDisplayCounts() {
        preferences.getString(storageKey, defaultValue = null)
            ?.deserializeFromJson<List<MessageDisplayCount>>()
            ?.let { displayCounts ->
                messageDisplayCounts.addAll(displayCounts)
            }
    }

    private fun saveDisplayCounts() {
        messageDisplayCounts
            .toList() // kotlinx.serialization supports serializing normal maps
            .serializeToJson()
            ?.let { json ->
                preferences.putString(storageKey, json)
            }
    }
}
