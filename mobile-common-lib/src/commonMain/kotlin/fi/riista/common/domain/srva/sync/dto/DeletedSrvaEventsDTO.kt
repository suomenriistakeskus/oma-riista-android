package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.dto.LocalDateTimeDTO

@kotlinx.serialization.Serializable
data class DeletedSrvaEventsDTO(
    val latestEntry: LocalDateTimeDTO? = null,
    val entryIds: List<Long>,
)
