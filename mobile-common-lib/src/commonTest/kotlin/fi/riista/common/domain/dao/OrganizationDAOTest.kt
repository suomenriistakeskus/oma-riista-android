package fi.riista.common.domain.dao

import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedString
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals

class OrganizationDAOTest: DaoTest() {
    @Test
    fun `conversion from organization to dao`() {
        val organization = mockOrganization

        val dao = organization.toOrganizationDAO()
        assertEquals(organization.id, dao.id)
        assertEquals(organization.name.toLocalizedStringDAO(), dao.name)
        assertEquals(organization.officialCode, dao.officialCode)
    }

    @Test
    fun `conversion from dao to organization`() {
        val dao = mockOrganizationDAO

        val organization = dao.toOrganization()
        assertEquals(dao.id, organization.id)
        assertEquals(dao.name.toLocalizedString(), organization.name)
        assertEquals(dao.officialCode, organization.officialCode)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"id\":1,\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"officialCode\":\"official code\"}",
            actual = OrganizationDAO(
                id = 1,
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en"
                ),
                officialCode = "official code",
            ).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = OrganizationDAO(
                id = 1,
                name = LocalizedStringDAO(
                    fi = "fi",
                    sv = "sv",
                    en = "en"
                ),
                officialCode = "official code",
            ),
            actual =
            """
            {
                "id": 1,
                "name": {
                    "fi": "fi",
                    "sv": "sv",
                    "en": "en"
                },
                "officialCode": "official code"
            }
            """.deserializeFromJson<OrganizationDAO>(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, OrganizationDAO.DAO_VERSION)
    }
}