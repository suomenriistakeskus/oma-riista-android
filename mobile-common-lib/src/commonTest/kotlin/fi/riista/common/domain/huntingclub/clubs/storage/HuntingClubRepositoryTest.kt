package fi.riista.common.domain.huntingclub.clubs.storage

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.Organization
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.LocalizedString
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingClubRepositoryTest {
    @Test
    fun `conversion to DbOrganization and back`() {
        val organization = Organization(
            id = 46,
            name = LocalizedString(
                fi = "organization_name_fi",
                sv = "organization_name_sv",
                en = "organization_name_en"
            ),
            officialCode = "123456",
        )

        assertEquals(
            expected = organization,
            actual = organization.toDbOrganization().toOrganization()
        )
    }

    @Test
    fun `only missing organizations are added`() {
        val dbDriverFactory = createDatabaseDriverFactory()
        val databaseDriver = dbDriverFactory.createDriver()
        val database = RiistaDatabase(driver = databaseDriver)
        val repository = HuntingClubRepository(database)

        val organization = Organization(
            id = 46,
            name = LocalizedString("fi", "sv", null),
            officialCode = "123"
        )
        repository.clubQueries.insertOrganization(organization.toDbOrganization())

        assertEquals(1, repository.clubQueries.organizationIds().executeAsList().size)
        assertEquals(
            expected = "123",
            actual = repository.clubQueries
                .selectOrganizationByRemoteId(46)
                .executeAsOne()
                .organization_official_code
        )

        val newOrganizations = listOf(
            organization.copy(officialCode = "456"),
            Organization(
                id = 99,
                name = LocalizedString("fi", "sv", null),
                officialCode = "789"
            )
        )

        runBlocking {
            repository.addOrganizationsIfNotExists(newOrganizations)
        }

        assertEquals(2, repository.clubQueries.organizationIds().executeAsList().size)
        assertEquals(
            expected = "123",
            actual = repository.clubQueries
                .selectOrganizationByRemoteId(46)
                .executeAsOne()
                .organization_official_code
        )
        assertEquals(
            expected = "789",
            actual = repository.clubQueries
                .selectOrganizationByRemoteId(99)
                .executeAsOne()
                .organization_official_code
        )
    }
}
