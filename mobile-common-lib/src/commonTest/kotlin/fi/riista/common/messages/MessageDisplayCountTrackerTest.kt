package fi.riista.common.messages

import fi.riista.common.preferences.MockPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageDisplayCountTrackerTest {

    @Test
    fun testDisplayCountIsTracked() {
        val (_, tracker) = getTrackerAndPreferences(2)
        assertEquals(0, tracker.messageDisplayCounts.size)
        assertEquals(0, tracker.displayCountFor(MSG_ID))

        tracker.setDisplayCountFor(MSG_ID, 1)

        assertEquals(1, tracker.displayCountFor(MSG_ID))
        assertEquals(1, tracker.messageDisplayCounts.size)
        assertEquals(MSG_ID, tracker.messageDisplayCounts[0].messageId)
        assertEquals(1, tracker.messageDisplayCounts[0].displayCount)
    }

    @Test
    fun testDisplayCountCanBeIncremented() {
        val (_, tracker) = getTrackerAndPreferences(2)
        tracker.setDisplayCountFor(MSG_ID, 1)

        assertEquals(1, tracker.messageDisplayCounts.size)
        assertEquals(MSG_ID, tracker.messageDisplayCounts[0].messageId)
        assertEquals(1, tracker.messageDisplayCounts[0].displayCount)

        tracker.incrementDisplayCountFor(MSG_ID)
        assertEquals(1, tracker.messageDisplayCounts.size)
        assertEquals(MSG_ID, tracker.messageDisplayCounts[0].messageId)
        assertEquals(2, tracker.messageDisplayCounts[0].displayCount)
    }

    @Test
    fun testEarliestDisplayCountsAreForgotten() {
        val msgIds = listOf("1", "2", "3")

        val (_, tracker) = getTrackerAndPreferences(2)
        assertDisplayCounts(tracker, msgIds, listOf(0, 0, 0))

        tracker.setDisplayCountFor("1", 1)
        assertDisplayCounts(tracker, msgIds, listOf(1, 0, 0))

        tracker.setDisplayCountFor("2", 2)
        assertDisplayCounts(tracker, msgIds, listOf(1, 2, 0))

        tracker.setDisplayCountFor("3", 3)
        assertDisplayCounts(tracker, msgIds, listOf(0, 2, 3))

        // refresh "2" -> no longer earliest
        tracker.setDisplayCountFor("2", 2)
        assertDisplayCounts(tracker, msgIds, listOf(0, 2, 3))

        tracker.setDisplayCountFor("1", 1)
        assertDisplayCounts(tracker, msgIds, listOf(1, 2, 0))
    }

    private fun assertDisplayCounts(tracker: MessageDisplayCountTracker,
                                    msgIds: List<MessageId>,
                                    displayCounts: List<Int>) {
        assertEquals(msgIds.size, displayCounts.size)
        for (i in msgIds.indices) {
            assertEquals(tracker.displayCountFor(msgIds[i]), displayCounts[i])
        }
    }

    @Test
    fun testDisplayCountsAreLoadedFromPreferencesAsJson() {
        val preferences = MockPreferences()
        preferences.putString(MSG_STORAGE_KEY, "[{\"messageId\":\"$MSG_ID\",\"displayCount\":2}]")

        val (_, tracker) = getTrackerAndPreferences(2, preferences = preferences)
        assertEquals(1, tracker.messageDisplayCounts.size)
        assertEquals(MSG_ID, tracker.messageDisplayCounts[0].messageId)
        assertEquals(2, tracker.messageDisplayCounts[0].displayCount)
    }

    @Test
    fun testDisplayCountsAreStoredToPreferencesAsJson() {
        val (prefs, tracker) = getTrackerAndPreferences(2)
        assertEquals(0, prefs.container.size)
        tracker.setDisplayCountFor(MSG_ID, 1)
        assertEquals(1, prefs.container.size)

        var json = prefs.getString(MSG_STORAGE_KEY, null)
        assertEquals("[{\"messageId\":\"$MSG_ID\",\"displayCount\":1}]", json)

        tracker.incrementDisplayCountFor(MSG_ID)
        assertEquals(1, prefs.container.size)
        json = prefs.getString(MSG_STORAGE_KEY, null)
        assertEquals("[{\"messageId\":\"$MSG_ID\",\"displayCount\":2}]", json)
    }


    private fun getTrackerAndPreferences(
        maxDisplayCountsToTrack: Int,
        preferences: MockPreferences = MockPreferences()
    ): Pair<MockPreferences, MessageDisplayCountTracker> {
        return Pair(preferences,
                    MessageDisplayCountTracker(
                            preferences = preferences,
                            maxDisplayCountsToTrack = maxDisplayCountsToTrack,
                            storageKey = MSG_STORAGE_KEY
                    )
        )
    }

    companion object {
        private const val MSG_ID: MessageId = "msg"
        private const val MSG_STORAGE_KEY = "prefs_display_counts"
    }
}
