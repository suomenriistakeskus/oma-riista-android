package fi.riista.common.domain.huntingControl.model

import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.ShootingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalizedString

@kotlinx.serialization.Serializable
data class HuntingControlHunterInfo(
    val name: String,
    val dateOfBirth: LocalDate?,
    val homeMunicipality: LocalizedString?,
    val hunterNumber: HunterNumber?,
    val huntingLicenseActive: Boolean,
    val huntingLicenseDateOfPayment: LocalDate?,
    val shootingTests: List<ShootingTest>,
)
