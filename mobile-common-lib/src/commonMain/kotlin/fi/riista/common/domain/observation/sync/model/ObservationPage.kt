package fi.riista.common.domain.observation.sync.model

import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.model.LocalDateTime

data class ObservationPage(
    val content: List<CommonObservation>,
    val latestEntry: LocalDateTime?,
    val hasMore: Boolean,
)
