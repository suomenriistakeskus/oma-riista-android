package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.dto.OrganizationDTO
import fi.riista.common.domain.dto.toOrganization
import fi.riista.common.domain.huntingControl.sync.model.LoadRhyHuntingControlEvents
import fi.riista.common.logging.Logger
import kotlinx.serialization.Serializable

@Serializable
data class LoadRhyHuntingControlEventsDTO(
    val specVersion: Int,
    val rhy: OrganizationDTO,
    val gameWardens: List<GameWardenDTO>,
    val events: List<HuntingControlEventDTO>,
)

fun LoadRhyHuntingControlEventsDTO.toLoadRhyHuntingControlEvents(logger: Logger): LoadRhyHuntingControlEvents {
    return LoadRhyHuntingControlEvents(
        specVersion = specVersion,
        rhy = rhy.toOrganization(),
        gameWardens = gameWardens.mapNotNull { warden -> warden.toGameWarden() },
        events = events.mapNotNull { event -> event.toLoadHuntingControlEvent(logger) }
    )
}
