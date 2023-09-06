package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OccupationDAOTest: DaoTest() {
    @Test
    fun `conversion from occupation to dao`() {
        val occupation = mockOccupation

        val dao = occupation.toOccupationDAO()
        assertNotNull(dao)
        assertEquals(occupation.id, dao.id)
        assertEquals(occupation.occupationType.rawBackendEnumValue, dao.occupationType)
        assertEquals(occupation.name.toLocalizedStringDAO(), dao.name)
        assertEquals(occupation.beginDate?.toStringISO8601(), dao.beginDate)
        assertEquals(occupation.endDate?.toStringISO8601(), dao.endDate)
        assertEquals(occupation.organisation.toOrganizationDAO(), dao.organisation)
    }

    @Test
    fun `conversion from dao to occupation`() {
        val dao = mockOccupationDAO

        val occupation = dao.toOccupation()
        assertEquals(dao.id, occupation.id)
        assertEquals(dao.occupationType.toBackendEnum(), occupation.occupationType)
        assertEquals(dao.name.toLocalizedString(), occupation.name)
        assertEquals(dao.beginDate?.toLocalDate(), occupation.beginDate)
        assertEquals(dao.endDate?.toLocalDate(), occupation.endDate)
        assertEquals(dao.organisation.toOrganization(), occupation.organisation)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"id\":1,\"occupationType\":\"occupationType\"," +
                    "\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"beginDate\":\"beginDate\"," +
                    "\"endDate\":\"endDate\",\"organisation\":{\"id\":1," +
                    "\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"officialCode\":\"official code\"}}",
            actual = OccupationDAO(
                id = 1,
                occupationType = "occupationType",
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en",
                ),
                beginDate = "beginDate",
                endDate = "endDate",
                organisation = OrganizationDAO(
                    id = 1,
                    name = LocalizedStringDAO(
                        fi = "fi",
                        sv = "sv",
                        en = "en"
                    ),
                    officialCode = "official code",
                ),
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"id\":1,\"occupationType\":\"occupationType\"," +
                    "\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"beginDate\":null," +
                    "\"endDate\":null,\"organisation\":{\"id\":1," +
                    "\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"officialCode\":\"official code\"}}",
            actual = OccupationDAO(
                id = 1,
                occupationType = "occupationType",
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en",
                ),
                beginDate = null,
                endDate = null,
                organisation = OrganizationDAO(
                    id = 1,
                    name = LocalizedStringDAO(
                        fi = "fi",
                        sv = "sv",
                        en = "en"
                    ),
                    officialCode = "official code",
                ),
            ).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = OccupationDAO(
                id = 1,
                occupationType = "occupationType",
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en",
                ),
                beginDate = "beginDate",
                endDate = "endDate",
                organisation = OrganizationDAO(
                    id = 1,
                    name = LocalizedStringDAO(
                        fi = "fi",
                        sv = "sv",
                        en = "en"
                    ),
                    officialCode = "official code",
                ),
            ),
            actual =
            """
            {
                "id": 1,
                "occupationType": "occupationType",
                "name": {
                    "fi": "fi",
                    "sv": "sv",
                    "en": "en"
                },
                "beginDate": "beginDate",
                "endDate": "endDate",
                "organisation": {
                    "id": 1,
                    "name": {
                        "fi": "fi",
                        "sv": "sv",
                        "en": "en"
                    },
                    "officialCode": "official code"
                }
            }
            """.deserializeFromJson<OccupationDAO>(),
        )

        assertEquals(
            expected = OccupationDAO(
                id = 1,
                occupationType = "occupationType",
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en",
                ),
                beginDate = null,
                endDate = null,
                organisation = OrganizationDAO(
                    id = 1,
                    name = LocalizedStringDAO(
                        fi = "fi",
                        sv = "sv",
                        en = "en"
                    ),
                    officialCode = "official code",
                ),
            ),
            actual =
            """
            {
                "id": 1,
                "occupationType": "occupationType",
                "name": {
                    "fi": "fi",
                    "sv": "sv",
                    "en": "en"
                },
                "beginDate": null,
                "endDate": null,
                "organisation": {
                    "id": 1,
                    "name": {
                        "fi": "fi",
                        "sv": "sv",
                        "en": "en"
                    },
                    "officialCode": "official code"
                }
            }
            """.deserializeFromJson<OccupationDAO>(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, OccupationDAO.DAO_VERSION)
    }
}