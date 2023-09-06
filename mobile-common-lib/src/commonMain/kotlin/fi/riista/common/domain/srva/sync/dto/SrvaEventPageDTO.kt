package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.srva.sync.model.SrvaEventPage
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SrvaEventPageDTO(
    val content: List<SrvaEventDTO>,
    val latestEntry: LocalDateTimeDTO? = null,
    val hasMore: Boolean? = null,
)

fun SrvaEventPageDTO.toSrvaEventPage(): SrvaEventPage {
    return SrvaEventPage(
        content = content.mapNotNull { event -> event.toCommonSrvaEvent() },
        latestEntry = latestEntry?.toLocalDateTime(),
        hasMore = hasMore ?: false,
    )
}
