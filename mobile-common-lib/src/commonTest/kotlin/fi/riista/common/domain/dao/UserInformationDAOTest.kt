package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalDateDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals

class UserInformationDAOTest: DaoTest() {
    @Test
    fun `conversion from dto to dao`() {
        val userInformation = mockUserInfo

        val dao = userInformation.toUserInformationDAO()
        assertEquals(userInformation.username, dao.username)
        assertEquals(userInformation.id, dao.id)
        assertEquals(userInformation.firstName, dao.firstName)
        assertEquals(userInformation.lastName, dao.lastName)
        assertEquals(userInformation.unregisterRequestedTime?.toStringISO8601(), dao.unregisterRequestedTime)
        assertEquals(userInformation.birthDate?.toStringISO8601(), dao.birthDate)
        assertEquals(userInformation.address?.toAddressDAO(), dao.address)
        assertEquals(userInformation.homeMunicipality.toLocalizedStringDAO(), dao.homeMunicipality)
        assertEquals(userInformation.rhy?.toOrganizationDAO(), dao.rhy)
        assertEquals(userInformation.hunterNumber, dao.hunterNumber)
        assertEquals(userInformation.hunterExamDate?.toStringISO8601(), dao.hunterExamDate)
        assertEquals(userInformation.huntingCardStart?.toStringISO8601(), dao.huntingCardStart)
        assertEquals(userInformation.huntingCardEnd?.toStringISO8601(), dao.huntingCardEnd)
        assertEquals(userInformation.huntingBanStart?.toStringISO8601(), dao.huntingBanStart)
        assertEquals(userInformation.huntingBanEnd?.toStringISO8601(), dao.huntingBanEnd)
        assertEquals(userInformation.huntingCardValidNow, dao.huntingCardValidNow)
        assertEquals(userInformation.qrCode, dao.qrCode)
        assertEquals(userInformation.timestamp, dao.timestamp)
        assertEquals(userInformation.shootingTests.map { it.toShootingTestDAO() }, dao.shootingTests)
        assertEquals(userInformation.occupations.map { it.toOccupationDAO() }, dao.occupations)
        assertEquals(userInformation.enableSrva, dao.enableSrva)
        assertEquals(userInformation.enableShootingTests, dao.enableShootingTests)
        assertEquals(userInformation.deerPilotUser, dao.deerPilotUser)
    }

    @Test
    fun `conversion from dao to dto`() {
        val dao = createDAO()

        val userInformation = dao.toUserInformation()
        assertEquals(dao.username, userInformation.username)
        assertEquals(dao.id, userInformation.id)
        assertEquals(dao.firstName, userInformation.firstName)
        assertEquals(dao.lastName, userInformation.lastName)
        assertEquals(dao.unregisterRequestedTime?.toLocalDateTime(), userInformation.unregisterRequestedTime)
        assertEquals(dao.birthDate?.toLocalDate(), userInformation.birthDate)
        assertEquals(dao.address?.toAddress(), userInformation.address)
        assertEquals(dao.homeMunicipality.toLocalizedString(), userInformation.homeMunicipality)
        assertEquals(dao.rhy?.toOrganization(), userInformation.rhy)
        assertEquals(dao.hunterNumber, userInformation.hunterNumber)
        assertEquals(dao.hunterExamDate?.toLocalDate(), userInformation.hunterExamDate)
        assertEquals(dao.huntingCardStart?.toLocalDate(), userInformation.huntingCardStart)
        assertEquals(dao.huntingCardEnd?.toLocalDate(), userInformation.huntingCardEnd)
        assertEquals(dao.huntingBanStart?.toLocalDate(), userInformation.huntingBanStart)
        assertEquals(dao.huntingBanEnd?.toLocalDate(), userInformation.huntingBanEnd)
        assertEquals(dao.huntingCardValidNow, userInformation.huntingCardValidNow)
        assertEquals(dao.qrCode, userInformation.qrCode)
        assertEquals(dao.timestamp, userInformation.timestamp)
        assertEquals(dao.shootingTests.mapNotNull { it.toShootingTest() }, userInformation.shootingTests)
        assertEquals(dao.occupations.map { it.toOccupation() }, userInformation.occupations)
        assertEquals(dao.enableSrva, userInformation.enableSrva)
        assertEquals(dao.enableShootingTests, userInformation.enableShootingTests)
        assertEquals(dao.deerPilotUser, userInformation.deerPilotUser)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"username\":\"username\",\"id\":123,\"firstName\":\"firstName\",\"lastName\":\"lastName\"," +
                    "\"unregisterRequestedTime\":\"2023-01-15T12:00:00\",\"birthDate\":\"1980-01-01\"," +
                    "\"address\":{\"id\":1,\"rev\":2,\"editable\":true,\"streetAddress\":\"streetAddress\"," +
                    "\"postalCode\":\"postalCode\",\"city\":\"city\",\"country\":\"country\"}," +
                    "\"homeMunicipality\":{\"fi\":\"home_fi\",\"sv\":\"home_sv\",\"en\":\"home_en\"}," +
                    "\"rhy\":{\"id\":1,\"name\":{\"fi\":\"organization_fi\",\"sv\":\"organization_sv\",\"en\":\"organization_en\"}," +
                    "\"officialCode\":\"official code\"},\"hunterNumber\":\"88888888\",\"hunterExamDate\":\"2023-01-01\"," +
                    "\"huntingCardStart\":\"2023-01-02\",\"huntingCardEnd\":\"2023-01-20\",\"huntingBanStart\":\"2023-01-21\"," +
                    "\"huntingBanEnd\":\"2023-01-23\",\"huntingCardValidNow\":true,\"qrCode\":\"qrCode\"," +
                    "\"timestamp\":\"timestamp\",\"shootingTests\":[{\"rhyCode\":\"rhyCode\",\"rhyName\":\"rhyName\"," +
                    "\"type\":\"BEAR\",\"typeName\":\"typeName\",\"begin\":\"2023-05-01\",\"end\":\"2023-05-31\"," +
                    "\"expired\":false}],\"occupations\":[{\"id\":1,\"occupationType\":\"AMPUMAKOKEEN_VASTAANOTTAJA\"," +
                    "\"name\":{\"fi\":\"occupation_fi\",\"sv\":\"occupation_sv\",\"en\":\"occupation_en\"}," +
                    "\"beginDate\":\"2023-01-01\",\"endDate\":\"2023-01-31\",\"organisation\":{\"id\":1," +
                    "\"name\":{\"fi\":\"organization_fi\",\"sv\":\"organization_sv\",\"en\":\"organization_en\"}," +
                    "\"officialCode\":\"official code\"}}],\"enableSrva\":true,\"enableShootingTests\":true,\"deerPilotUser\":true}",
            actual = createDAO().serializeToJson(),
        )

        assertEquals(
            expected = "{\"username\":\"username\",\"id\":123,\"firstName\":\"firstName\",\"lastName\":\"lastName\"," +
                    "\"unregisterRequestedTime\":\"2023-01-15T12:00:00\",\"birthDate\":null," +
                    "\"address\":{\"id\":1,\"rev\":2,\"editable\":true,\"streetAddress\":\"streetAddress\"," +
                    "\"postalCode\":\"postalCode\",\"city\":\"city\",\"country\":\"country\"}," +
                    "\"homeMunicipality\":{\"fi\":\"home_fi\",\"sv\":\"home_sv\",\"en\":\"home_en\"}," +
                    "\"rhy\":{\"id\":1,\"name\":{\"fi\":\"organization_fi\",\"sv\":\"organization_sv\",\"en\":\"organization_en\"}," +
                    "\"officialCode\":\"official code\"},\"hunterNumber\":\"88888888\",\"hunterExamDate\":\"2023-01-01\"," +
                    "\"huntingCardStart\":\"2023-01-02\",\"huntingCardEnd\":\"2023-01-20\",\"huntingBanStart\":\"2023-01-21\"," +
                    "\"huntingBanEnd\":\"2023-01-23\",\"huntingCardValidNow\":true,\"qrCode\":\"qrCode\"," +
                    "\"timestamp\":\"timestamp\",\"shootingTests\":[{\"rhyCode\":\"rhyCode\",\"rhyName\":\"rhyName\"," +
                    "\"type\":\"BEAR\",\"typeName\":\"typeName\",\"begin\":\"2023-05-01\",\"end\":\"2023-05-31\"," +
                    "\"expired\":false}],\"occupations\":[{\"id\":1,\"occupationType\":\"AMPUMAKOKEEN_VASTAANOTTAJA\"," +
                    "\"name\":{\"fi\":\"occupation_fi\",\"sv\":\"occupation_sv\",\"en\":\"occupation_en\"}," +
                    "\"beginDate\":\"2023-01-01\",\"endDate\":\"2023-01-31\",\"organisation\":{\"id\":1," +
                    "\"name\":{\"fi\":\"organization_fi\",\"sv\":\"organization_sv\",\"en\":\"organization_en\"}," +
                    "\"officialCode\":\"official code\"}}],\"enableSrva\":true,\"enableShootingTests\":true,\"deerPilotUser\":true}",
            actual = createDAO(birthDate = null).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = createDAO(),
            actual =
                """
                {
                    "username": "username",
                    "id": 123,
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "unregisterRequestedTime": "2023-01-15T12:00:00",
                    "birthDate": "1980-01-01",
                    "address": {
                        "id": 1,
                        "rev": 2,
                        "editable": true,
                        "streetAddress": "streetAddress",
                        "postalCode": "postalCode",
                        "city": "city",
                        "country": "country"
                    },
                    "homeMunicipality": {
                        "fi": "home_fi",
                        "sv": "home_sv",
                        "en": "home_en"
                    },
                    "rhy": {
                        "id": 1,
                        "name": {
                            "fi": "organization_fi",
                            "sv": "organization_sv",
                            "en": "organization_en"
                        },
                        "officialCode": "official code"
                    },
                    "hunterNumber": "88888888",
                    "hunterExamDate": "2023-01-01",
                    "huntingCardStart": "2023-01-02",
                    "huntingCardEnd": "2023-01-20",
                    "huntingBanStart": "2023-01-21",
                    "huntingBanEnd": "2023-01-23",
                    "huntingCardValidNow": true,
                    "qrCode": "qrCode",
                    "timestamp": "timestamp",
                    "shootingTests": [
                        {
                            "rhyCode": "rhyCode",
                            "rhyName": "rhyName",
                            "type": "BEAR",
                            "typeName": "typeName",
                            "begin": "2023-05-01",
                            "end": "2023-05-31",
                            "expired": false
                        }
                    ],
                    "occupations": [
                        {
                            "id": 1,
                            "occupationType": "AMPUMAKOKEEN_VASTAANOTTAJA",
                            "name": {
                                "fi": "occupation_fi",
                                "sv": "occupation_sv",
                                "en": "occupation_en"
                            },
                            "beginDate": "2023-01-01",
                            "endDate": "2023-01-31",
                            "organisation": {
                                "id": 1,
                                "name": {
                                    "fi": "organization_fi",
                                    "sv": "organization_sv",
                                    "en": "organization_en"
                                },
                                "officialCode": "official code"
                            }
                        }
                    ],
                    "enableSrva": true,
                    "enableShootingTests": true,
                    "deerPilotUser": true
                }
                """.deserializeFromJson()
        )
    }

    @Test
    fun `deserialization from json with null or empty values`() {
        assertEquals(
            expected = createDAO(birthDate = null, clearShootingTests = true),
            actual =
                """
                {
                    "username": "username",
                    "id": 123,
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "unregisterRequestedTime": "2023-01-15T12:00:00",
                    "birthDate": null,
                    "address": {
                        "id": 1,
                        "rev": 2,
                        "editable": true,
                        "streetAddress": "streetAddress",
                        "postalCode": "postalCode",
                        "city": "city",
                        "country": "country"
                    },
                    "homeMunicipality": {
                        "fi": "home_fi",
                        "sv": "home_sv",
                        "en": "home_en"
                    },
                    "rhy": {
                        "id": 1,
                        "name": {
                            "fi": "organization_fi",
                            "sv": "organization_sv",
                            "en": "organization_en"
                        },
                        "officialCode": "official code"
                    },
                    "hunterNumber": "88888888",
                    "hunterExamDate": "2023-01-01",
                    "huntingCardStart": "2023-01-02",
                    "huntingCardEnd": "2023-01-20",
                    "huntingBanStart": "2023-01-21",
                    "huntingBanEnd": "2023-01-23",
                    "huntingCardValidNow": true,
                    "qrCode": "qrCode",
                    "timestamp": "timestamp",
                    "shootingTests": [],
                    "occupations": [
                        {
                            "id": 1,
                            "occupationType": "AMPUMAKOKEEN_VASTAANOTTAJA",
                            "name": {
                                "fi": "occupation_fi",
                                "sv": "occupation_sv",
                                "en": "occupation_en"
                            },
                            "beginDate": "2023-01-01",
                            "endDate": "2023-01-31",
                            "organisation": {
                                "id": 1,
                                "name": {
                                    "fi": "organization_fi",
                                    "sv": "organization_sv",
                                    "en": "organization_en"
                                },
                                "officialCode": "official code"
                            }
                        }
                    ],
                    "enableSrva": true,
                    "enableShootingTests": true,
                    "deerPilotUser": true
                }
                """.deserializeFromJson()
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, UserInformationDAO.DAO_VERSION)
    }

    private fun createDAO(
        birthDate: LocalDateDAO? = mockUserInformationDAO.birthDate,
        clearShootingTests: Boolean = false,
    ): UserInformationDAO {
        return mockUserInformationDAO.copy(
            birthDate = birthDate,
            shootingTests = mockUserInformationDAO.shootingTests.takeUnless { clearShootingTests } ?: emptyList()
        )
    }
}
