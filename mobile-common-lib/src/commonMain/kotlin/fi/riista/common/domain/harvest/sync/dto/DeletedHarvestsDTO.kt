package fi.riista.common.domain.harvest.sync.dto

import fi.riista.common.dto.LocalDateTimeDTO
import kotlinx.serialization.Serializable

@Serializable
data class DeletedHarvestsDTO(
    val latestEntry: LocalDateTimeDTO? = null,
    val entryIds: List<Long>,
)
