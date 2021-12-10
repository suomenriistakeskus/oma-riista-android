package fi.riista.common.groupHunting.model

import fi.riista.common.model.CoordinateBounds

data class HuntingGroupArea(
    // Internal id for the area. Used on backend.
    val areaId: Long,

    // Id used for enabling e.g. map layers.
    val externalId: String,

    // The bounding box coordinates for the area
    val bounds: CoordinateBounds,
)