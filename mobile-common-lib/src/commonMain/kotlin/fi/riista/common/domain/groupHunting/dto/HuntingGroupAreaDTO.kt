package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.dto.CoordinateBoundsDTO
import fi.riista.common.dto.toCoordinateBounds
import kotlinx.serialization.Serializable

@Serializable
data class HuntingGroupAreaDTO(
    val areaId: Long,
    val externalId: String,
    val bounds: CoordinateBoundsDTO,
)

fun HuntingGroupAreaDTO.toHuntingGroupArea() =
    HuntingGroupArea(areaId = areaId,
                     externalId = externalId,
                     bounds = bounds.toCoordinateBounds()
    )