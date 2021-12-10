package fi.riista.common.messages

import fi.riista.common.PlatformName
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.MessageDTO
import fi.riista.common.dto.MessageTargetApplicationVersionsDTO
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.JsonHelper
import kotlin.test.*

class MessageHandlerTest {

    @Test
    fun thereIsNoMessageInitially() {
        assertNull(handler().getMessage())
    }

    @Test
    fun messageDoesntExistIfParsingFails() {
        // ensure valid message json by serializing it ourselves
        val messageJson =
            "{" +
                // id is required but rest of the fields are optional
                "\"id_missing\": 99" +
            "}"

        val msgHandler = handler()
        msgHandler.parseMessageFromJson(messageJson)

        val parsedMessage = msgHandler.getMessage()
        assertNull(parsedMessage)
    }

    @Test
    fun messageExistsIfParsingSucceeds() {
        val messageJson = messageJson(msgId = MSG_ID)
        val msgHandler = handler()

        msgHandler.parseMessageFromJson(messageJson)

        val parsedMessage = msgHandler.getMessage()
        assertNotNull(parsedMessage)
        assertEquals(MSG_ID, parsedMessage.id)
    }

    @Test
    fun messageCanBeDisplayedManyTimes() {
        val messageJson = messageJson()
        val msgHandler = handler()

        msgHandler.parseMessageFromJson(messageJson)

        // parsing should have succeeded
        assertNotNull(msgHandler.getMessage())

        // first display attempt
        assertNotNull(msgHandler.getMessageIfNotDisplayCountLimited())

        // second display attempt
        assertNotNull(msgHandler.getMessageIfNotDisplayCountLimited())
    }

    @Test
    fun messageCanOnlyBeDisplayedOnceEver() {
        val messageJson = messageJson(maxDisplayCount = 1)
        val preferences = MockPreferences()

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNotNull(it.getMessageIfNotDisplayCountLimited())
            it.incrementMessageAutomaticDisplayCount(MSG_ID)
        }

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNull(it.getMessageIfNotDisplayCountLimited())
            assertNotNull(it.getMessage())
        }
    }

    @Test
    fun messageCanBeDisplayedManyTimesIfDisplayCountNotIncremented() {
        val messageJson = messageJson(maxDisplayCount = 1)
        val preferences = MockPreferences()

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNotNull(it.getMessageIfNotDisplayCountLimited())
            // comment out display count increment in order to underline that it's not done
            // it.incrementMessageDisplayCount(MSG_ID)
        }

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNotNull(it.getMessageIfNotDisplayCountLimited())
        }
    }

    @Test
    fun messageCanOnlyBeDisplayedTwiceEver() {
        val messageJson = messageJson(maxDisplayCount = 2)
        val preferences = MockPreferences()

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNotNull(it.getMessageIfNotDisplayCountLimited())
            it.incrementMessageAutomaticDisplayCount(MSG_ID)
        }

        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNotNull(it.getMessageIfNotDisplayCountLimited())
            it.incrementMessageAutomaticDisplayCount(MSG_ID)
        }

        // the third time
        handler(preferences = preferences).let {
            it.parseMessageFromJson(messageJson)
            assertNull(it.getMessageIfNotDisplayCountLimited())
            assertNotNull(it.getMessage())
        }
    }

    @Test
    fun messageCanBeDisplayedAlways() {
        val messageJson = messageJson(maxDisplayCount = -1)
        val preferences = MockPreferences()

        for (i in 1..99) {
            handler(preferences = preferences).let {
                it.parseMessageFromJson(messageJson)
                assertNotNull(it.getMessageIfNotDisplayCountLimited())
                it.incrementMessageAutomaticDisplayCount(MSG_ID)
            }
        }
    }

    @Test
    fun messageCanBeTargetedToAppVersion() {
        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(iosVersions = listOf(APP_VERSION))
            )
            assertNotNull(handler.getMessageIfNotDisplayCountLimited(), "ios-1")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(iosVersions = null)
            )
            assertNotNull(handler.getMessageIfNotDisplayCountLimited(), "ios-2")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(iosVersions = listOf())
            )
            assertNull(handler.getMessageIfNotDisplayCountLimited(), "ios-3")
        }

        handler(appVersion = "$APP_VERSION.1", platformName = PlatformName.IOS).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(iosVersions = listOf(APP_VERSION))
            )
            assertNull(handler.getMessageIfNotDisplayCountLimited(), "ios-4")
        }


        // same for android..

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(androidVersions = listOf(APP_VERSION))
            )
            assertNotNull(handler.getMessageIfNotDisplayCountLimited(), "android-1")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(androidVersions = null)
            )
            assertNotNull(handler.getMessageIfNotDisplayCountLimited(), "android-2")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(androidVersions = listOf())
            )
            assertNull(handler.getMessageIfNotDisplayCountLimited(), "android-3")
        }

        handler(appVersion = "$APP_VERSION.1", platformName = PlatformName.ANDROID).let { handler ->
            handler.parseMessageFromJson(
                    messageJson(androidVersions = listOf(APP_VERSION))
            )
            assertNull(handler.getMessageIfNotDisplayCountLimited(), "android-4")
        }
    }

    private fun messageJson(
        msgId: MessageId = MSG_ID,
        maxDisplayCount: Int = 1,
        iosVersions: List<String>? = null,
        androidVersions: List<String>? = null
    ): String {
        val msg = MessageDTO(
                id = msgId,
                maxDisplayCount = maxDisplayCount,
                targetApplicationVersions = MessageTargetApplicationVersionsDTO(
                        ios = iosVersions,
                        android = androidVersions,
                ),
                title = LocalizedStringDTO("Title"),
                message = LocalizedStringDTO("Message")
        )

        return JsonHelper.serializeToJsonUnsafe(msg)
    }

    private fun handler(appVersion: String = APP_VERSION,
                        platformName: PlatformName = PlatformName.ANDROID,
                        preferences: Preferences = MockPreferences()
    ): MessageHandler {
        return MessageHandler(appVersion, platformName, "MessageHandler", preferences)
    }

    companion object {
        private const val MSG_ID: MessageId = "msg"
        private const val APP_VERSION: String = "2.4.0"
    }
}
