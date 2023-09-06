package fi.riista.common.domain.model

import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalizedString

data class UserInformation(
    val username: String,
    val id: Long?,
    val firstName: String,
    val lastName: String,

    /**
     * The timestamp (if any) when user has requested account unregistration.
     */
    val unregisterRequestedTime: LocalDateTime?,

    // possibly null if there's no Social Security Number for the user in the backend
    val birthDate: LocalDate?,

    val address: Address?,
    val homeMunicipality: LocalizedString,
    val rhy: Organization?,

    val hunterNumber: HunterNumber?,
    val hunterExamDate: LocalDate?,
    val huntingCardStart: LocalDate?,
    val huntingCardEnd: LocalDate?,
    val huntingBanStart: LocalDate?,
    val huntingBanEnd: LocalDate?,
    val huntingCardValidNow: Boolean,

    val qrCode: String?,

    // the time when the UserInfo was obtained from the backend
    val timestamp: DateTimeDTO,

    val shootingTests: List<ShootingTest>,
    val occupations: List<Occupation>,

    val enableSrva: Boolean,
    val enableShootingTests: Boolean,
    val deerPilotUser: Boolean,
)
