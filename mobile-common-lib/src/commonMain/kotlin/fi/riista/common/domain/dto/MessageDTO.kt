package fi.riista.common.domain.dto

import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalizedString
import fi.riista.common.messages.Message
import fi.riista.common.messages.MessageId
import fi.riista.common.messages.MessageLink
import kotlinx.serialization.Serializable


@Serializable
internal class MessageDTO(
    // the unique id for the message
    val id: MessageId,

    // how many times the message is allowed to be displayed. -1 == infinitely
    val maxDisplayCount: Int? = DEFAULT_MESSAGE_DISPLAY_COUNT,

    // the application versions allowed to display the message.
    // Not taken into account if null.
    val targetApplicationVersions: MessageTargetApplicationVersionsDTO? = null,

    // for dialog title
    val title: LocalizedStringDTO? = null,

    // to be used as dialog message. Can contain line breaks, html probably doesn't work
    val message: LocalizedStringDTO? = null,

    // Should the app be closed or further usage prevented when message is dismissed?
    val preventFurtherAppUsage: Boolean? = null,

    // Possible link information
    val link: MessageLinkDTO? = null,
)

@Serializable
internal data class MessageLinkDTO(
    val name: LocalizedStringDTO,
    val url: LocalizedStringDTO
)

private const val DEFAULT_MESSAGE_DISPLAY_COUNT = 1

internal fun MessageDTO.toMessage() : Message {
    return Message(
        id = id,
        maxDisplayCount = maxDisplayCount ?: DEFAULT_MESSAGE_DISPLAY_COUNT,
        targetApplicationVersions = targetApplicationVersions?.toTargetApplicationVersions(),
        title = title?.toLocalizedString(),
        message = message?.toLocalizedString(),
        preventFurtherAppUsage = preventFurtherAppUsage ?: false,
        link = link?.toMessageLink()
    )
}

internal fun MessageLinkDTO.toMessageLink() =
    MessageLink(
        name = name.toLocalizedString(),
        url = url.toLocalizedString(),
    )


