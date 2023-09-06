package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.domain.model.Address
import fi.riista.common.domain.model.Occupation
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.ShootingTest
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.domain.model.UserInformation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.toBackendEnum

open class DaoTest {

    private fun mockLocalizedString(id: String = "") = LocalizedString(
        fi = "${id}_fi",
        sv = "${id}_sv",
        en = "${id}_en",
    )

    private fun mockLocalizedStringDAO(id: String = "") = LocalizedStringDAO(
        fi = "${id}_fi",
        sv = "${id}_sv",
        en = "${id}_en",
    )

    internal val mockAddress = Address(
        id = 1,
        rev = 2,
        editable = true,
        streetAddress = "streetAddress",
        postalCode = "postalCode",
        city = "city",
        country = "country",
    )

    internal val mockAddressDAO = AddressDAO(
        id = 1,
        rev = 2,
        editable = true,
        streetAddress = "streetAddress",
        postalCode = "postalCode",
        city = "city",
        country = "country",
    )

    internal val mockOrganizationDAO = OrganizationDAO(
        id = 1,
        name = mockLocalizedStringDAO("organization"),
        officialCode = "official code",
    )

    internal val mockOrganization = Organization(
        id = 1,
        name = mockLocalizedString("organization"),
        officialCode = "official code",
    )

    internal val mockOccupationDAO = OccupationDAO(
        id = 1,
        occupationType = OccupationType.SHOOTING_TEST_OFFICIAL.rawBackendEnumValue,
        name = mockLocalizedStringDAO("occupation"),
        beginDate = "2023-01-01",
        endDate = "2023-01-31",
        organisation = mockOrganizationDAO,
    )

    internal val mockOccupation = Occupation(
        id = 1,
        occupationType = OccupationType.SHOOTING_TEST_OFFICIAL.toBackendEnum(),
        name = mockLocalizedString("occupation"),
        beginDate = LocalDate(2023, 1, 1),
        endDate = LocalDate(2023, 1, 31),
        organisation = mockOrganization,
    )

    internal val mockShootingTestDAO = ShootingTestDAO(
        rhyCode = "rhyCode",
        rhyName = "rhyName",
        type = ShootingTestType.BEAR.rawBackendEnumValue,
        typeName = "typeName",
        begin = "2023-05-01",
        end = "2023-05-31",
        expired = false,
    )

    internal val mockShootingTest = ShootingTest(
        rhyCode = "rhyCode",
        rhyName = "rhyName",
        type = ShootingTestType.BEAR.toBackendEnum(),
        typeName = "typeName",
        begin = LocalDate(2023, 5, 1),
        end = LocalDate(2023, 5, 31),
        expired = false,
    )

    internal val mockUserInformationDAO = UserInformationDAO(
        username = "username",
        id = 123L,
        firstName = "firstName",
        lastName = "lastName",
        unregisterRequestedTime = "2023-01-15T12:00:00",
        birthDate = "1980-01-01",
        address = mockAddressDAO,
        homeMunicipality = mockLocalizedStringDAO("home"),
        rhy = mockOrganizationDAO,
        hunterNumber = "88888888",
        hunterExamDate = "2023-01-01",
        huntingCardStart = "2023-01-02",
        huntingCardEnd = "2023-01-20",
        huntingBanStart = "2023-01-21",
        huntingBanEnd = "2023-01-23",
        huntingCardValidNow = true,
        qrCode = "qrCode",
        timestamp = "timestamp",
        shootingTests = listOf(mockShootingTestDAO),
        occupations = listOf(mockOccupationDAO),
        enableSrva = true,
        enableShootingTests = true,
        deerPilotUser = true,
    )

    internal val mockUserInfo = UserInformation(
        username = "username",
        id = 123L,
        firstName = "firstName",
        lastName = "lastName",
        unregisterRequestedTime = LocalDateTime(2023, 1, 15, 12, 0, 0),
        birthDate = LocalDate(1980, 1, 1),
        address = mockAddress,
        homeMunicipality = mockLocalizedString("home"),
        rhy = mockOrganization,
        hunterNumber = "88888888",
        hunterExamDate = LocalDate(2023, 1, 1),
        huntingCardStart = LocalDate(2023, 1, 2),
        huntingCardEnd = LocalDate(2023, 1, 20),
        huntingBanStart = LocalDate(2023, 1, 21),
        huntingBanEnd = LocalDate(2023, 1, 23),
        huntingCardValidNow = true,
        qrCode = "qrCode",
        timestamp = "timestamp",
        shootingTests = listOf(mockShootingTest),
        occupations = listOf(mockOccupation),
        enableSrva = true,
        enableShootingTests = true,
        deerPilotUser = true,
    )
}
