package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestPerson
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestPersonDTO(
    val id: Long,
    val firstName: String? = null,
    val lastName: String? = null,
    val hunterNumber: String? = null,
    val dateOfBirth: LocalDateDTO? = null,
    val registrationStatus: String? = null,
    val selectedShootingTestTypes: SelectedShootingTestTypesDTO,
)

fun ShootingTestPersonDTO.toShootingTestPerson() =
    CommonShootingTestPerson(
        id = id,
        firstName = firstName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        dateOfBirth = dateOfBirth?.toLocalDate(),
        registrationStatus = registrationStatus.toBackendEnum(),
        selectedShootingTestTypes = selectedShootingTestTypes.toSelectedShootingTestTypes(),
    )
