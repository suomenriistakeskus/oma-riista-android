package fi.riista.common.domain.dao

import fi.riista.common.database.dao.DateTimeDAO
import fi.riista.common.database.dao.LocalDateDAO
import fi.riista.common.database.dao.LocalDateTimeDAO
import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.domain.model.UserInformation
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class UserInformationDAO(
    val username: String,
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val unregisterRequestedTime: LocalDateTimeDAO?,
    val birthDate: LocalDateDAO?,
    val address: AddressDAO?,
    val homeMunicipality: LocalizedStringDAO,
    val rhy: OrganizationDAO?,
    val hunterNumber: HunterNumberDAO?,
    val hunterExamDate: LocalDateDAO?,
    val huntingCardStart: LocalDateDAO?,
    val huntingCardEnd: LocalDateDAO?,
    val huntingBanStart: LocalDateDAO?,
    val huntingBanEnd: LocalDateDAO?,
    val huntingCardValidNow: Boolean,
    val qrCode: String?,
    val timestamp: DateTimeDAO,
    val shootingTests: List<ShootingTestDAO>,
    val occupations: List<OccupationDAO>,
    val enableSrva: Boolean,
    val enableShootingTests: Boolean,
    val deerPilotUser: Boolean,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun UserInformation.toUserInformationDAO(): UserInformationDAO {
    return UserInformationDAO(
        username = username,
        id = id,
        firstName = firstName,
        lastName = lastName,
        unregisterRequestedTime = unregisterRequestedTime?.toStringISO8601(),
        birthDate = birthDate?.toStringISO8601(),
        address = address?.toAddressDAO(),
        homeMunicipality = homeMunicipality.toLocalizedStringDAO(),
        rhy = rhy?.toOrganizationDAO(),
        hunterNumber = hunterNumber,
        hunterExamDate = hunterExamDate?.toStringISO8601(),
        huntingCardStart = huntingCardStart?.toStringISO8601(),
        huntingCardEnd = huntingCardEnd?.toStringISO8601(),
        huntingBanStart = huntingBanStart?.toStringISO8601(),
        huntingBanEnd = huntingBanEnd?.toStringISO8601(),
        huntingCardValidNow = huntingCardValidNow,
        qrCode = qrCode,
        timestamp = timestamp,
        shootingTests = shootingTests.mapNotNull { it.toShootingTestDAO() },
        occupations = occupations.mapNotNull { it.toOccupationDAO() },
        enableSrva = enableSrva,
        enableShootingTests = enableShootingTests,
        deerPilotUser = deerPilotUser,
    )
}

internal fun UserInformationDAO.toUserInformation(): UserInformation {
    return UserInformation(
        username = username,
        id = id,
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
