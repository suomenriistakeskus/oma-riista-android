package fi.riista.common.domain.harvest.sync.dto

import fi.riista.common.domain.harvest.sync.model.HarvestPage
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class HarvestPageDTO(
    val content: List<HarvestDTO>,
    val latestEntry: LocalDateTimeDTO? = null,
    val hasMore: Boolean? = null,
)

internal fun HarvestPageDTO.toHarvestPage(): HarvestPage {
    return HarvestPage(
        content = content.mapNotNull { it.toCommonHarvest() },
        latestEntry = latestEntry?.toLocalDateTime(),
        hasMore = hasMore ?: false,
    )
}
