package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.huntingControl.sync.model.GameWarden
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import kotlinx.serialization.Serializable

@Serializable
data class GameWardenDTO(
    val inspector: InspectorDTO,
    val beginDate: LocalDateDTO,
    val endDate: LocalDateDTO,
)

fun GameWardenDTO.toGameWarden(): GameWarden? {
    val beginDate = beginDate.toLocalDate()
    val endDate = endDate.toLocalDate()
    if (beginDate == null || endDate == null) {
        return null
    }

    return GameWarden(
        inspector = inspector.toHuntingControlEventInspector(),
        beginDate = beginDate,
        endDate = endDate,
    )
}
