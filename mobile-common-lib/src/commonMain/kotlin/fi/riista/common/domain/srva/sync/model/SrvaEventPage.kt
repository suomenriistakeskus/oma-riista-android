package fi.riista.common.domain.srva.sync.model

import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.model.LocalDateTime

data class SrvaEventPage(
    val content: List<CommonSrvaEvent>,
    val latestEntry: LocalDateTime? = null,
    val hasMore: Boolean,
)
