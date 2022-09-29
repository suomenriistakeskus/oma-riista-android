package fi.riista.common.domain.srva.model

import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaEventAuthor(
    val id: Long,
    val revision: Long,
    val byName: String?,
    val lastName: String?,
)
