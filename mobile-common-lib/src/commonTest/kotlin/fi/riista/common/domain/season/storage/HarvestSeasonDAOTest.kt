package fi.riista.common.domain.season.storage

import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedStringDTO
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals

class HarvestSeasonDAOTest {
    @Test
    fun `conversion from dto to dao`() {
        val dto = HarvestSeasonDTO(
            name = LocalizedStringDTO("fi", "sv", "en"),
            gameSpeciesCode = 1,
            beginDate = "2023-01-01",
            endDate = "2023-01-20",
            endOfReportingDate = "2023-01-31",
            beginDate2 = "2023-03-01",
            endDate2 = "2023-03-20",
            endOfReportingDate2 = "2023-03-31",
        )

        val dao = dto.toHarvestSeasonDAO()
        assertEquals(dto.name, dao.name?.toLocalizedStringDTO())
        assertEquals(dto.gameSpeciesCode, dao.gameSpeciesCode)
        assertEquals(dto.beginDate, dao.beginDate)
        assertEquals(dto.endDate, dao.endDate)
        assertEquals(dto.endOfReportingDate, dao.endOfReportingDate)
        assertEquals(dto.beginDate2, dao.beginDate2)
        assertEquals(dto.endDate2, dao.endDate2)
        assertEquals(dto.endOfReportingDate2, dao.endOfReportingDate2)
    }

    @Test
    fun `conversion from dao to dto`() {
        val dao = HarvestSeasonDAO(
            name = LocalizedStringDAO("fi", "sv", "en"),
            gameSpeciesCode = 1,
            beginDate = "2023-01-01",
            endDate = "2023-01-20",
            endOfReportingDate = "2023-01-31",
            beginDate2 = "2023-03-01",
            endDate2 = "2023-03-20",
            endOfReportingDate2 = "2023-03-31",
        )

        val dto = dao.toHarvestSeasonDTO()
        assertEquals(dao.name, dto.name?.toLocalizedStringDAO())
        assertEquals(dao.gameSpeciesCode, dto.gameSpeciesCode)
        assertEquals(dao.beginDate, dto.beginDate)
        assertEquals(dao.endDate, dto.endDate)
        assertEquals(dao.endOfReportingDate, dto.endOfReportingDate)
        assertEquals(dao.beginDate2, dto.beginDate2)
        assertEquals(dao.endDate2, dto.endDate2)
        assertEquals(dao.endOfReportingDate2, dto.endOfReportingDate2)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"gameSpeciesCode\":1," +
                    "\"beginDate\":\"2023-01-01\",\"endDate\":\"2023-01-20\",\"endOfReportingDate\":\"2023-01-31\"," +
                    "\"beginDate2\":\"2023-03-01\",\"endDate2\":\"2023-03-20\",\"endOfReportingDate2\":\"2023-03-31\"}",
            actual = HarvestSeasonDAO(
                name = LocalizedStringDAO("fi", "sv", "en"),
                gameSpeciesCode = 1,
                beginDate = "2023-01-01",
                endDate = "2023-01-20",
                endOfReportingDate = "2023-01-31",
                beginDate2 = "2023-03-01",
                endDate2 = "2023-03-20",
                endOfReportingDate2 = "2023-03-31",
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"name\":{\"fi\":\"fi\",\"sv\":\"sv\",\"en\":\"en\"},\"gameSpeciesCode\":1," +
                    "\"beginDate\":\"2023-01-01\",\"endDate\":\"2023-01-20\",\"endOfReportingDate\":\"2023-01-31\"," +
                    "\"beginDate2\":null,\"endDate2\":null,\"endOfReportingDate2\":null}",
            actual = HarvestSeasonDAO(
                name = LocalizedStringDAO("fi", "sv", "en"),
                gameSpeciesCode = 1,
                beginDate = "2023-01-01",
                endDate = "2023-01-20",
                endOfReportingDate = "2023-01-31",
                beginDate2 = null,
                endDate2 = null,
                endOfReportingDate2 = null,
            ).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = HarvestSeasonDAO(
                name = LocalizedStringDAO("fi", "sv", "en"),
                gameSpeciesCode = 1,
                beginDate = "2023-01-01",
                endDate = "2023-01-20",
                endOfReportingDate = "2023-01-31",
                beginDate2 = "2023-03-01",
                endDate2 = "2023-03-20",
                endOfReportingDate2 = "2023-03-31",
            ),
            actual =
                """
                {
                    "name": {
                        "fi": "fi",
                        "sv": "sv",
                        "en": "en"
                    },
                    "gameSpeciesCode": 1,
                    "beginDate": "2023-01-01",
                    "endDate": "2023-01-20",
                    "endOfReportingDate": "2023-01-31",
                    "beginDate2": "2023-03-01",
                    "endDate2": "2023-03-20",
                    "endOfReportingDate2": "2023-03-31"
                }
                """.deserializeFromJson(),
        )

        assertEquals(
            expected = HarvestSeasonDAO(
                name = LocalizedStringDAO("fi", "sv", "en"),
                gameSpeciesCode = 1,
                beginDate = "2023-01-01",
                endDate = "2023-01-20",
                endOfReportingDate = "2023-01-31",
                beginDate2 = null,
                endDate2 = null,
                endOfReportingDate2 = null,
            ),
            actual =
                """
                {
                    "name": {
                        "fi": "fi",
                        "sv": "sv",
                        "en": "en"
                    },
                    "gameSpeciesCode": 1,
                    "beginDate": "2023-01-01",
                    "endDate": "2023-01-20",
                    "endOfReportingDate": "2023-01-31",
                    "beginDate2": null,
                    "endDate2": null,
                    "endOfReportingDate2": null
                }
                """.deserializeFromJson(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, HarvestSeasonDAO.DAO_VERSION)
    }
}