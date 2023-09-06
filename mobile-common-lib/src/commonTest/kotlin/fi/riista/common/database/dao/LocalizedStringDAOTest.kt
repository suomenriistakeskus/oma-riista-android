package fi.riista.common.database.dao

import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.model.LocalizedString
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedStringDAOTest {
    @Test
    fun `conversion from dto to dao`() {
        val dto = LocalizedStringDTO(
            fi = "fi",
            sv = "sv",
            en = "en"
        )

        val dao = dto.toLocalizedStringDAO()
        assertEquals(dto.fi, dao.fi)
        assertEquals(dto.sv, dao.sv)
        assertEquals(dto.en, dao.en)
    }

    @Test
    fun `conversion from dao to dto`() {
        val dao = LocalizedStringDAO(
            fi = "fi",
            sv = "sv",
            en = "en"
        )

        val dto = dao.toLocalizedStringDTO()
        assertEquals(dao.fi, dto.fi)
        assertEquals(dao.sv, dto.sv)
        assertEquals(dao.en, dto.en)
    }

    @Test
    fun `conversion from model to dao`() {
        val localizedString = LocalizedString(
            fi = "fi",
            sv = "sv",
            en = "en"
        )

        val dao = localizedString.toLocalizedStringDAO()
        assertEquals(localizedString.fi, dao.fi)
        assertEquals(localizedString.sv, dao.sv)
        assertEquals(localizedString.en, dao.en)
    }

    @Test
    fun `conversion from dao to model`() {
        val dao = LocalizedStringDAO(
            fi = "fi",
            sv = "sv",
            en = "en"
        )

        val localizedString = dao.toLocalizedString()
        assertEquals(dao.fi, localizedString.fi)
        assertEquals(dao.sv, localizedString.sv)
        assertEquals(dao.en, localizedString.en)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"}",
            actual = LocalizedStringDAO(
                fi = "fi",
                sv = "sv",
                en = "en"
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":null}",
            actual = LocalizedStringDAO(
                fi = "fi",
                sv = "sv",
                en = null
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"fi\":null,\"sv\":null,\"en\":null}",
            actual = LocalizedStringDTO().toLocalizedStringDAO().serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = LocalizedStringDAO(
                fi = "fi",
                sv = "sv",
                en = "en"
            ),
            actual = "{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"}".deserializeFromJson<LocalizedStringDAO>(),
        )

        assertEquals(
            expected = LocalizedStringDAO(
                fi = "fi",
                sv = "sv",
                en = null
            ),
            actual = "{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":null}".deserializeFromJson<LocalizedStringDAO>(),
        )

        assertEquals(
            expected = LocalizedStringDTO().toLocalizedStringDAO(),
            actual = "{\"fi\":null,\"sv\":null,\"en\":null}".deserializeFromJson<LocalizedStringDAO>(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, LocalizedStringDAO.DAO_VERSION)
    }
}