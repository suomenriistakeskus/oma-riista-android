package fi.riista.common.domain.shootingTest.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestParticipantCreateDTO(
    val hunterNumber: String,
    val selectedTypes: SelectedShootingTestTypesDTO,
)
