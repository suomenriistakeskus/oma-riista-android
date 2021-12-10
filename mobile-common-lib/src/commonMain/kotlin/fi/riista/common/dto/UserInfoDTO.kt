package fi.riista.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDTO(
    val username: String,
    val firstName: String,
    val lastName: String,
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

    // deprecated: contains harvestYears and observationYears
    val gameDiaryYears: Set<Int>,
    val harvestYears: Set<Int>,
    val observationYears: Set<Int>,

    val shootingTests: List<ShootingTestDTO>,

    val occupations: List<OccupationDTO>,

    val enableSrva: Boolean,
    val enableShootingTests: Boolean,
    val deerPilotUser: Boolean,
)
