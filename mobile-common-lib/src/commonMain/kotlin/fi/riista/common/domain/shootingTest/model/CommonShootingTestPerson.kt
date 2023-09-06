package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate

data class CommonShootingTestPerson(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val hunterNumber: String?,
    val dateOfBirth: LocalDate?,
    val registrationStatus: BackendEnum<ShootingTestRegistrationStatus>,
    val selectedShootingTestTypes: SelectedShootingTestTypes,
)
