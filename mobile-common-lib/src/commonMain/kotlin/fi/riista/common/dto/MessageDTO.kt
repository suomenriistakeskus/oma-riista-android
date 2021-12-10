package fi.riista.common.dto

import fi.riista.common.messages.Message
import fi.riista.common.messages.MessageId
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
)

private const val DEFAULT_MESSAGE_DISPLAY_COUNT = 1

internal fun MessageDTO.toMessage() : Message {
    return Message(id,
                   maxDisplayCount ?: DEFAULT_MESSAGE_DISPLAY_COUNT,
                   targetApplicationVersions?.toTargetApplicationVersions(),
                   title?.toLocalizedString(),
                   message?.toLocalizedString())
}

