package fi.riista.common.domain.observation.sync.dto

import fi.riista.common.domain.observation.sync.model.ObservationPage
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ObservationPageDTO(
    val content: List<ObservationDTO>,
    val latestEntry: LocalDateTimeDTO? = null,
    val hasMore: Boolean? = null,
)

fun ObservationPageDTO.toObservationPage(): ObservationPage {
    return ObservationPage(
        content = content.mapNotNull { observation -> observation.toCommonObservation() },
        latestEntry = latestEntry?.toLocalDateTime(),
        hasMore = hasMore ?: false,
    )
}
