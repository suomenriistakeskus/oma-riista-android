package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.huntingControl.sync.model.GameWarden
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import kotlinx.serialization.Serializable

@Serializable
data class GameWardenDTO(
    val inspector: InspectorDTO,
    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,
)

fun GameWardenDTO.toGameWarden(): GameWarden {
    return GameWarden(
        inspector = inspector.toHuntingControlEventInspector(),
        beginDate = beginDate?.toLocalDate(),
        endDate = endDate?.toLocalDate(),
    )
}
