package fi.riista.common.messages

import fi.riista.common.PlatformName
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.domain.dto.MessageDTO
import fi.riista.common.domain.dto.MessageLinkDTO
import fi.riista.common.domain.dto.MessageTargetApplicationVersionsDTO
import fi.riista.common.model.Language
import fi.riista.common.model.LocalizedString
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.JsonHelper
import fi.riista.common.util.toLocalizedStringDTO
import kotlin.test.*

class AppStartupMessageHandlerTest {

    @Test
    fun thereIsNoMessageInitially() {
        assertNull(handler().delegateMessageHandler.getMessage())
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
        msgHandler.parseAppStartupMessageFromJson(messageJson)

        val parsedMessage = msgHandler.delegateMessageHandler.getMessage()
        assertNull(parsedMessage)
    }

    @Test
    fun messageExistsIfParsingSucceeds() {
        val messageJson = messageJson(msgId = MSG_ID)
        val msgHandler = handler()

        msgHandler.parseAppStartupMessageFromJson(messageJson)

        val parsedMessage = msgHandler.delegateMessageHandler.getMessage()
        assertNotNull(parsedMessage)
        assertEquals(MSG_ID, parsedMessage.id)
    }

    @Test
    fun messageCanOnlyBeDisplayedOncePerStartup() {
        val messageJson = messageJson()
        val msgHandler = handler()

        msgHandler.parseAppStartupMessageFromJson(messageJson)

        // parsing should have succeeded
        assertNotNull(msgHandler.delegateMessageHandler.getMessage())

        // first display attempt
        assertNotNull(msgHandler.getAppStartupMessageToBeDisplayed())

        // message should still exist but it is no longer returned
        assertNotNull(msgHandler.delegateMessageHandler.getMessage())
        assertTrue(msgHandler.startupMessageDisplayAttempted.value)
        assertNull(msgHandler.getAppStartupMessageToBeDisplayed())
    }

    @Test
    fun messageCanOnlyBeDisplayedOnceEver() {
        val messageJson = messageJson(maxDisplayCount = 1)
        val preferences = MockPreferences()

        // simulates the first time the app is run
        handler(preferences = preferences).let {
            it.parseAppStartupMessageFromJson(messageJson)
            assertNotNull(it.getAppStartupMessageToBeDisplayed())
        }

        // the second time the app is run
        handler(preferences = preferences).let {
            it.parseAppStartupMessageFromJson(messageJson)
            assertNull(it.getAppStartupMessageToBeDisplayed())
        }
    }

    @Test
    fun messageCanOnlyBeDisplayedTwiceEver() {
        val messageJson = messageJson(maxDisplayCount = 2)
        val preferences = MockPreferences()

        // simulates the first time the app is run
        handler(preferences = preferences).let {
            it.parseAppStartupMessageFromJson(messageJson)
            assertNotNull(it.getAppStartupMessageToBeDisplayed())
            assertNull(it.getAppStartupMessageToBeDisplayed())
        }

        // the second time the app is run
        handler(preferences = preferences).let {
            it.parseAppStartupMessageFromJson(messageJson)
            assertNotNull(it.getAppStartupMessageToBeDisplayed())
            assertNull(it.getAppStartupMessageToBeDisplayed())
        }

        // the third time
        handler(preferences = preferences).let {
            it.parseAppStartupMessageFromJson(messageJson)
            assertNull(it.getAppStartupMessageToBeDisplayed())
        }
    }

    @Test
    fun messageCanOnlyBeDisplayedAlways() {
        val messageJson = messageJson(maxDisplayCount = -1)
        val preferences = MockPreferences()

        // let's not test to infinity but at least more than few..
        for (i in 1..99) {
            handler(preferences = preferences).let {
                it.parseAppStartupMessageFromJson(messageJson)
                assertNotNull(it.getAppStartupMessageToBeDisplayed())
                assertNull(it.getAppStartupMessageToBeDisplayed())
            }
        }
    }

    @Test
    fun messageCanCauseFurtherAppUsageToBePrevented() {
        val msgHandler = handler()

        msgHandler.parseAppStartupMessageFromJson(messageJson(preventFurtherAppUsage = true))
        assertEquals(true, msgHandler.delegateMessageHandler.getMessage()?.preventFurtherAppUsage, "true")

        msgHandler.parseAppStartupMessageFromJson(messageJson(preventFurtherAppUsage = false))
        assertEquals(false, msgHandler.delegateMessageHandler.getMessage()?.preventFurtherAppUsage, "false")

        msgHandler.parseAppStartupMessageFromJson(messageJson(preventFurtherAppUsage = null))
        assertEquals(false, msgHandler.delegateMessageHandler.getMessage()?.preventFurtherAppUsage, "null")
    }

    @Test
    fun messageCanContainLink() {
        val msgHandler = handler()

        msgHandler.parseAppStartupMessageFromJson(messageJson(link = null))
        assertNull(msgHandler.delegateMessageHandler.getMessage()?.link, "null")

        msgHandler.parseAppStartupMessageFromJson(messageJson(link = MessageLink(
            name = LocalizedString("foo", null, null),
            url = LocalizedString("bar", null, null)
        )))

        with (assertNotNull(msgHandler.delegateMessageHandler.getMessage()?.link, "null")) {
            assertEquals("foo", localizedName(Language.FI))
            assertEquals("bar", localizedUrl(Language.FI))
        }

    }

    @Test
    fun messageCanBeTargetedToAppVersion() {
        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(iosVersions = listOf(APP_VERSION))
            )
            assertNotNull(handler.getAppStartupMessageToBeDisplayed(), "ios-1")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(iosVersions = null)
            )
            assertNotNull(handler.getAppStartupMessageToBeDisplayed(), "ios-2")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.IOS).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(iosVersions = listOf())
            )
            assertNull(handler.getAppStartupMessageToBeDisplayed(), "ios-3")
        }

        handler(appVersion = "$APP_VERSION.1", platformName = PlatformName.IOS).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(iosVersions = listOf(APP_VERSION))
            )
            assertNull(handler.getAppStartupMessageToBeDisplayed(), "ios-4")
        }


        // same for android..

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(androidVersions = listOf(APP_VERSION))
            )
            assertNotNull(handler.getAppStartupMessageToBeDisplayed(), "android-1")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(androidVersions = null)
            )
            assertNotNull(handler.getAppStartupMessageToBeDisplayed(), "android-2")
        }

        handler(appVersion = APP_VERSION, platformName = PlatformName.ANDROID).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(androidVersions = listOf())
            )
            assertNull(handler.getAppStartupMessageToBeDisplayed(), "android-3")
        }

        handler(appVersion = "$APP_VERSION.1", platformName = PlatformName.ANDROID).let { handler ->
            handler.parseAppStartupMessageFromJson(
                    messageJson(androidVersions = listOf(APP_VERSION))
            )
            assertNull(handler.getAppStartupMessageToBeDisplayed(), "android-4")
        }
    }

    private fun messageJson(
        msgId: MessageId = MSG_ID,
        maxDisplayCount: Int = 1,
        iosVersions: List<String>? = null,
        androidVersions: List<String>? = null,
        preventFurtherAppUsage: Boolean? = null,
        link: MessageLink? = null,
    ): String {
        val msg = MessageDTO(
            id = msgId,
            maxDisplayCount = maxDisplayCount,
            targetApplicationVersions = MessageTargetApplicationVersionsDTO(
                    ios = iosVersions,
                    android = androidVersions,
            ),
            title = LocalizedStringDTO("Title"),
            message = LocalizedStringDTO("Message"),
            preventFurtherAppUsage = preventFurtherAppUsage,
            link = link?.let {
                MessageLinkDTO(
                    name = it.name.toLocalizedStringDTO(),
                    url =  it.url.toLocalizedStringDTO()
                )
            }
        )

        return JsonHelper.serializeToJsonUnsafe(msg)
    }

    private fun handler(appVersion: String = APP_VERSION,
                        platformName: PlatformName = PlatformName.ANDROID,
                        preferences: Preferences = MockPreferences()
    ): AppStartupMessageHandler {
        return AppStartupMessageHandler(appVersion, platformName, preferences)
    }

    companion object {
        private const val MSG_ID: MessageId = "msg"
        private const val APP_VERSION: String = "2.4.0"
    }
}
