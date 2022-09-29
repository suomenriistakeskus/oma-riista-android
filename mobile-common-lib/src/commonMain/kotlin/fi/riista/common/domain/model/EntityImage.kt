package fi.riista.common.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EntityImage(
    val serverId: String?,
    val localIdentifier: String?,
    val localUrl: String?,
    val status: Status,
) {
    enum class Status {
        // local, not yet uploaded. Status for newly created local images.
        LOCAL,
        // local, not yet uploaded + TO BE REMOVED
        LOCAL_TO_BE_REMOVED,
        // uploaded to backend, possibly local
        UPLOADED,
    }
}