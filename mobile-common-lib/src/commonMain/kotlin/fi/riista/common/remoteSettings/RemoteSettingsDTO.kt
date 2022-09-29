package fi.riista.common.remoteSettings

import fi.riista.common.domain.dto.HunterNumberDTO
import kotlinx.serialization.Serializable

@Serializable
data class RemoteSettingsDTO(
    val groupHunting: GroupHuntingSettingsDTO,
)

@Serializable
data class GroupHuntingSettingsDTO(
    val enabledForAll: Boolean,
    val enabledForHunters: List<HunterNumberDTO>,
)
