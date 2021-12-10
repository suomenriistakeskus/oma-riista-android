package fi.riista.common.model

import kotlinx.serialization.Serializable

@Serializable
data class ETRSCoordinate(
    val x: Long,
    val y: Long,
)