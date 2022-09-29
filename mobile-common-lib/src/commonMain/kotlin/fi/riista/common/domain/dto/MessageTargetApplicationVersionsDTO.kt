package fi.riista.common.domain.dto

import fi.riista.common.messages.MessageTargetApplicationVersions
import kotlinx.serialization.Serializable

@Serializable
data class MessageTargetApplicationVersionsDTO(
    // the application versions allowed to display the message e.g. ["2.4.3", "2.4.4"]
    // Not taken into account if null.
    val ios: List<String>? = null,

    // the application versions allowed to display the message e.g. ["2.4.5"]
    // Not taken into account if null.
    val android: List<String>? = null
)

internal fun MessageTargetApplicationVersionsDTO.toTargetApplicationVersions() =
    MessageTargetApplicationVersions(ios, android)