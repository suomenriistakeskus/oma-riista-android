package fi.riista.common.domain.huntingControl.dto

import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.ShootingTestDTO
import fi.riista.common.domain.dto.toShootingTest
import fi.riista.common.domain.huntingControl.model.HuntingControlHunterInfo
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalizedString
import kotlinx.serialization.Serializable

@Serializable
data class HuntingControlHunterInfoDTO(
    val name: String,
    val dateOfBirth: LocalDateDTO? = null,
    val homeMunicipality: LocalizedStringDTO? = null,
    val hunterNumber: HunterNumberDTO? = null,
    val huntingLicenseActive: Boolean,
    val huntingLicenseDateOfPayment: LocalDateDTO? = null,
    val shootingTests: List<ShootingTestDTO>,
)

fun HuntingControlHunterInfoDTO.toHuntingControlHunterInfo(): HuntingControlHunterInfo {
    return HuntingControlHunterInfo(
        name = name,
        dateOfBirth = dateOfBirth?.toLocalDate(),
        homeMunicipality = homeMunicipality?.toLocalizedString(),
        hunterNumber = hunterNumber,
        huntingLicenseActive = huntingLicenseActive,
        huntingLicenseDateOfPayment = huntingLicenseDateOfPayment?.toLocalDate(),
        shootingTests = shootingTests.mapNotNull { test -> test.toShootingTest() }
    )
}
