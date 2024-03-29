package fi.riista.common.messages

import fi.riista.common.model.Language
import fi.riista.common.model.LocalizedString
import kotlinx.serialization.Serializable

typealias MessageId = String

@Serializable
data class Message(
    // the unique id for the message
    val id: MessageId,

    // how many times the message is allowed to be displayed automatically.
    // - automatically i.e. without user interaction
    // - value of -1 means that there's no limit
    val maxDisplayCount: Int,

    // the application versions allowed to display the message.
    // Not taken into account if null.
    val targetApplicationVersions: MessageTargetApplicationVersions?,

    // for dialog title
    val title: LocalizedString?,

    // to be used as dialog message. Can contain line breaks, html probably doesn't work
    val message: LocalizedString?,

    // Should the app be closed or further usage prevented when message is dismissed?
    val preventFurtherAppUsage: Boolean,

    // Possible link information
    val link: MessageLink?,
) {
    fun localizedTitle(languageCode: String): String? = title?.localized(languageCode)
    fun localizedTitle(language: Language): String? = title?.localized(language)

    fun localizedMessage(languageCode: String): String? = message?.localized(languageCode)
    fun localizedMessage(language: Language): String? = message?.localized(language)

    companion object {
        const val DISPLAY_COUNT_NOT_LIMITED: Int = -1
    }
}

@Serializable
data class MessageLink(
    val name: LocalizedString,
    val url: LocalizedString
) {
    fun localizedName(languageCode: String): String? = name.localized(languageCode)
    fun localizedName(language: Language): String? = name.localized(language)

    fun localizedUrl(languageCode: String): String? = url.localized(languageCode)
    fun localizedUrl(language: Language): String? = url.localized(language)
}
