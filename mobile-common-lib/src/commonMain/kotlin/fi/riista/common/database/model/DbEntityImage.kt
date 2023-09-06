package fi.riista.common.database.model

import fi.riista.common.domain.model.EntityImage
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.serialization.Serializable

@Serializable
internal data class DbEntityImage(
    val serverId: String?,
    val localIdentifier: String?,
    val localUrl: String?,
    val status: String,
)

private val EntityImage.Status.databaseValue: String
    get() {
        return when (this) {
            EntityImage.Status.LOCAL -> "LOCAL"
            EntityImage.Status.LOCAL_TO_BE_REMOVED -> "LOCAL_TO_BE_REMOVED"
            EntityImage.Status.UPLOADED -> "UPLOADED"
        }
    }

internal fun DbEntityImage.toEntityImage() = EntityImage(
    serverId = serverId,
    localIdentifier = localIdentifier,
    localUrl = localUrl,
    status = EntityImage.Status.values().firstOrNull { it.databaseValue == status } ?: EntityImage.Status.LOCAL_TO_BE_REMOVED
)

internal fun EntityImage.toDbEntityImage() = DbEntityImage(
    serverId = serverId,
    localIdentifier = localIdentifier,
    localUrl = localUrl,
    status = status.databaseValue,
)

internal fun List<EntityImage>.toDbEntityImageString(): String? {
    if (this.isEmpty()) {
        return null
    }
    return this
        // Don't save images with status LOCAL_TO_BE_REMOVED to database
        .filter { it.status != EntityImage.Status.LOCAL_TO_BE_REMOVED }
        .map { it.toDbEntityImage() }
        .serializeToJson()
}

internal fun String.toEntityImages(): List<EntityImage>? {
    return this.deserializeFromJson<List<DbEntityImage>>()?.map { it.toEntityImage() }
}
