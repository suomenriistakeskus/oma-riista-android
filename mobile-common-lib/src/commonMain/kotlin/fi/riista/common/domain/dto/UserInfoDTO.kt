package fi.riista.common.domain.dto

import fi.riista.common.domain.model.UserInformation
import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.dto.toLocalizedString
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDTO(
    val username: String,
    /**
     * The id of the matching person. Allowed to be null when migrating from application side
     * user information. Otherwise expected to exist.
     */
    val personId: Long? = null,
    val firstName: String,
    val lastName: String,

    /**
     * The timestamp (if any) when user has requested account unregistration.
     */
    val unregisterRequestedTime: LocalDateTimeDTO? = null,

    // possibly null if there's no Social Security Number for the user in the backend
    val birthDate: LocalDateDTO? = null,

    val address: AddressDTO? = null,
    val homeMunicipality: LocalizedStringDTO,
    val rhy: OrganizationDTO? = null,

    val hunterNumber: HunterNumberDTO? = null,
    val hunterExamDate: LocalDateDTO? = null,
    val huntingCardStart: LocalDateDTO? = null,
    val huntingCardEnd: LocalDateDTO? = null,
    val huntingBanStart: LocalDateDTO? = null,
    val huntingBanEnd: LocalDateDTO? = null,
    val huntingCardValidNow: Boolean,

    val qrCode: String? = null,

    // the time when the UserInfo was obtained from the backend
    val timestamp: DateTimeDTO,

    val shootingTests: List<ShootingTestDTO>,

    val occupations: List<OccupationDTO>,

    val enableSrva: Boolean,
    val enableShootingTests: Boolean,
    val deerPilotUser: Boolean,
)

internal fun UserInfoDTO.toUserInformation(): UserInformation {
    return UserInformation(
        username = username,
        id = personId,
        firstName = firstName,
        lastName = lastName,
        unregisterRequestedTime = unregisterRequestedTime?.toLocalDateTime(),
        birthDate = birthDate?.toLocalDate(),
        address = address?.toAddress(),
        homeMunicipality = homeMunicipality.toLocalizedString(),
        rhy = rhy?.toOrganization(),
        hunterNumber = hunterNumber,
        hunterExamDate = hunterExamDate?.toLocalDate(),
        huntingCardStart = huntingCardStart?.toLocalDate(),
        huntingCardEnd = huntingCardEnd?.toLocalDate(),
        huntingBanStart = huntingBanStart?.toLocalDate(),
        huntingBanEnd = huntingBanEnd?.toLocalDate(),
        huntingCardValidNow = huntingCardValidNow,
        qrCode = qrCode,
        timestamp = timestamp,
        shootingTests = shootingTests.mapNotNull { it.toShootingTest() },
        occupations = occupations.map { it.toOccupation() },
        enableSrva = enableSrva,
        enableShootingTests = enableShootingTests,
        deerPilotUser = deerPilotUser,
    )
}
