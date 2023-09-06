package fi.riista.common.domain.harvest.sync.model

import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.model.LocalDateTime

data class HarvestPage(
    val content: List<CommonHarvest>,
    val latestEntry: LocalDateTime?,
    val hasMore: Boolean,
)
