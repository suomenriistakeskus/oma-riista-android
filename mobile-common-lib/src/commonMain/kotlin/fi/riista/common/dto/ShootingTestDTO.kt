package fi.riista.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestDTO(
    val rhyCode: String,
    val rhyName: String,
    val type: ShootingTestTypeDTO,
    val typeName: String,

    val begin: LocalDateDTO,
    val end: LocalDateDTO,
    val expired: Boolean,
)
