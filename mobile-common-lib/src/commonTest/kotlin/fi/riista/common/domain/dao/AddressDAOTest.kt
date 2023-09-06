package fi.riista.common.domain.dao

import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressDAOTest: DaoTest() {
    @Test
    fun `conversion from address to dao`() {
        val address = mockAddress

        val dao = address.toAddressDAO()
        assertEquals(address.id, dao.id)
        assertEquals(address.rev, dao.rev)
        assertEquals(address.editable, dao.editable)
        assertEquals(address.streetAddress, dao.streetAddress)
        assertEquals(address.postalCode, dao.postalCode)
        assertEquals(address.city, dao.city)
        assertEquals(address.country, dao.country)
    }

    @Test
    fun `conversion from dao to address`() {
        val dao = mockAddressDAO

        val address = dao.toAddress()
        assertEquals(dao.id, address.id)
        assertEquals(dao.rev, address.rev)
        assertEquals(dao.editable, address.editable)
        assertEquals(dao.streetAddress, address.streetAddress)
        assertEquals(dao.postalCode, address.postalCode)
        assertEquals(dao.city, address.city)
        assertEquals(dao.country, address.country)
    }

    @Test
    fun `serialization to json`() {
        assertEquals(
            expected = "{\"id\":1,\"rev\":2,\"editable\":true,\"streetAddress\":\"streetAddress\"," +
                    "\"postalCode\":\"postalCode\",\"city\":\"city\",\"country\":\"country\"}",
            actual = AddressDAO(
                id = 1,
                rev = 2,
                editable = true,
                streetAddress = "streetAddress",
                postalCode = "postalCode",
                city = "city",
                country = "country",
            ).serializeToJson(),
        )

        assertEquals(
            expected = "{\"id\":1,\"rev\":2,\"editable\":true,\"streetAddress\":null," +
                    "\"postalCode\":null,\"city\":null,\"country\":null}",
            actual = AddressDAO(
                id = 1,
                rev = 2,
                editable = true,
                streetAddress = null,
                postalCode = null,
                city = null,
                country = null,
            ).serializeToJson(),
        )
    }

    @Test
    fun `deserialization from json`() {
        assertEquals(
            expected = AddressDAO(
                id = 1,
                rev = 2,
                editable = true,
                streetAddress = "streetAddress",
                postalCode = "postalCode",
                city = "city",
                country = "country",
            ),
            actual =
            """
            {
                "id": 1,
                "rev": 2,
                "editable": true,
                "streetAddress": "streetAddress",
                "postalCode": "postalCode",
                "city": "city",
                "country": "country"
            }
            """.deserializeFromJson<AddressDAO>(),
        )

        assertEquals(
            expected = AddressDAO(
                id = 1,
                rev = 2,
                editable = true,
                streetAddress = null,
                postalCode = null,
                city = null,
                country = null,
            ),
            actual =
            """
            {
                "id": 1,
                "rev": 2,
                "editable": true,
                "streetAddress": null,
                "postalCode": null,
                "city": null,
                "country": null
            }
            """.deserializeFromJson<AddressDAO>(),
        )
    }

    @Test
    fun `dao version is expected one`() {
        assertEquals(1, AddressDAO.DAO_VERSION)
    }
}