package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.CoordinateBoundsDTO
import fi.riista.common.dto.toCoordinateBounds
import fi.riista.common.groupHunting.model.HuntingGroupArea
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