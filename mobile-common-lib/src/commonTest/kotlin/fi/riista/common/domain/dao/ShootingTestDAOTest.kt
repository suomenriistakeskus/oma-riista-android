package fi.riista.common.domain.dao

import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShootingTestDAOTest: DaoTest() {
    @Test
    fun `conversion from shooting test to dao`() {
        val shootingTest = mockShootingTest

        val dao = shootingTest.toShootingTestDAO()
        assertNotNull(dao)
        assertEquals(shootingTest.rhyCode, dao.rhyCode)
        assertEquals(shootingTest.rhyName, dao.rhyName)
        assertEquals(shootingTest.type.rawBackendEnumValue, dao.type)
        assertEquals(shootingTest.typeName, dao.typeName)
        assertEquals(shootingTest.begin.toStringISO8601(), dao.begin)
        assertEquals(shootingTest.end.toStringISO8601(), dao.end)
        assertEquals(shootingTest.expired, dao.expired)
    }

    @Test
    fun `conversion from dao to shooting test`() {
        val dao = mockShootingTestDAO

        val shootingTest = dao.toShootingTest()
        assertNotNull(shootingTest)
        assertEquals(dao.rhyCode, shootingTest.rhyCode)
        assertEquals(dao.rhyName, shootingTest.rhyName)
        assertEquals(dao.type.toBackendEnum(), shootingTest.type)
        assertEquals(dao.typeName, shootingTest.typeName)
        assertEquals(dao.begin.toLocalDate(), shootingTest.begin)
        assertEquals(dao.end.toLocalDate(), shootingTest.end)
        assertEquals(dao.expired, shootingTest.expired)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"rhyCode\":\"rhyCode\",\"rhyName\":\"rhyName\",\"type\":\"type\",\"typeName\":\"typeName\"," +
                    "\"begin\":\"begin\",\"end\":\"end\",\"expired\":false}",
            actual = ShootingTestDAO(
                rhyCode = "rhyCode",
                rhyName = "rhyName",
                type = "type",
                typeName = "typeName",
                begin = "begin",
                end = "end",
                expired = false,
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"rhyCode\":null,\"rhyName\":null,\"type\":\"type\",\"typeName\":null," +
                    "\"begin\":\"begin\",\"end\":\"end\",\"expired\":false}",
            actual = ShootingTestDAO(
                rhyCode = null,
                rhyName = null,
                type = "type",
                typeName = null,
                begin = "begin",
                end = "end",
                expired = false,
            ).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = ShootingTestDAO(
                rhyCode = "rhyCode",
                rhyName = "rhyName",
                type = "type",
                typeName = "typeName",
                begin = "begin",
                end = "end",
                expired = false,
            ),
            actual =
            """
            {
                "rhyCode": "rhyCode",
                "rhyName": "rhyName",
                "type": "type",
                "typeName": "typeName",
                "begin": "begin",
                "end": "end",
                "expired": false
            }
            """.deserializeFromJson<ShootingTestDAO>(),
        )

        assertEquals(
            expected = ShootingTestDAO(
                rhyCode = null,
                rhyName = null,
                type = "type",
                typeName = null,
                begin = "begin",
                end = "end",
                expired = false,
            ),
            actual =
            """
            {
                "rhyCode": null,
                "rhyName": null,
                "type": "type",
                "typeName": null,
                "begin": "begin",
                "end": "end",
                "expired": false
            }
            """.deserializeFromJson<ShootingTestDAO>(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, ShootingTestDAO.DAO_VERSION)
    }
}