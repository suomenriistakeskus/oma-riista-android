package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.SelectedShootingTestTypes
import kotlinx.serialization.Serializable

@Serializable
data class SelectedShootingTestTypesDTO(
    val mooseTestIntended: Boolean = false,
    val bearTestIntended: Boolean = false,
    val roeDeerTestIntended: Boolean = false,
    val bowTestIntended: Boolean = false,
)

fun SelectedShootingTestTypesDTO.toSelectedShootingTestTypes() =
    SelectedShootingTestTypes(
        mooseTestIntended = mooseTestIntended,
        bearTestIntended = bearTestIntended,
        roeDeerTestIntended = roeDeerTestIntended,
        bowTestIntended = bowTestIntended,
    )
