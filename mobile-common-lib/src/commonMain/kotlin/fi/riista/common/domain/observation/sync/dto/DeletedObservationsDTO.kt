package fi.riista.common.domain.observation.sync.dto

import fi.riista.common.dto.LocalDateTimeDTO
import kotlinx.serialization.Serializable

@Serializable
data class DeletedObservationsDTO(
    val latestEntry: LocalDateTimeDTO? = null,
    val entryIds: List<Long>,
)
