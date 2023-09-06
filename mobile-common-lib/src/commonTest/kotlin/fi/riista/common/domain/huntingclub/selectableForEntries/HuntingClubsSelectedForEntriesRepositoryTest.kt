package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingclub.DbOrganization
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubRepository
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.util.contains
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HuntingClubsSelectedForEntriesRepositoryTest {

    @Test
    fun `selected club is fetched from harvest`() {
        val repository = createDatabase()
            .addHuntingClub(1)
            .addHarvest(username, 1, 1)
            .createRepository()

        val clubs = repository.getClubs(username)
        assertEquals(1, clubs.size)
        assertEquals(1, clubs.first().id)
    }

    @Test
    fun `club is returned only once`() {
        val repository = createDatabase()
            .addHuntingClub(1)
            .addHarvest(username, 1, 1)
            .addHarvest(username, 2, 1)
            .createRepository()

        val clubs = repository.getClubs(username)
        assertEquals(1, clubs.size)
        assertEquals(1, clubs.first().id)
    }

    @Test
    fun `clubs are returned from multiple harvests`() {
        val repository = createDatabase()
            .addHuntingClub(1)
            .addHuntingClub(2)
            .addHarvest(username, 1, 1)
            .addHarvest(username, 2, 2)
            .createRepository()

        val clubs = repository.getClubs(username)
        assertEquals(2, clubs.size)
        assertTrue(clubs.contains { it.id == 1L })
        assertTrue(clubs.contains { it.id == 2L })
    }

    @Test
    fun `clubs are returned from users own harvests`() {
        val repository = createDatabase()
            .addHuntingClub(1)
            .addHuntingClub(2)
            .addHarvest(username, 1, 1)
            .addHarvest("pentti", 2, 2)
            .createRepository()

        val clubs = repository.getClubs(username)
        assertEquals(1, clubs.size)
        assertEquals(1, clubs.first().id)
    }

    private fun createDatabase(): RiistaDatabase {
        val driver = createDatabaseDriverFactory().createDriver()
        return RiistaDatabase(driver)
    }

    private fun RiistaDatabase.addHuntingClub(
        organizationId: OrganizationId,
    ): RiistaDatabase {
        dbOrganizationQueries.insertOrganization(
            DbOrganization(
                organization_remote_id = organizationId,
                organization_official_code = (organizationId + 1234567).toString(),
                organization_name_fi = "fi_$organizationId",
                organization_name_sv = "sv_$organizationId",
                organization_name_en = "en_$organizationId",
            )
        )
        return this
    }

    private fun RiistaDatabase.addHarvest(
        username: String,
        harvestId: Long,
        selectedClubId: OrganizationId,
    ): RiistaDatabase {
        dbHarvestQueries.insert(
            username = username,
            remote_id = harvestId,
            rev = 0,
            mobile_client_ref_id = 1234,
            can_edit = true,
            modified = false,
            deleted = false,
            spec_version = Constants.HARVEST_SPEC_VERSION,
            game_species_code = 47503,
            specimens = "[]",
            amount = 1,
            point_of_time = "2023-06-20T08:51:31",
            description = "",
            harvest_report_done = false,
            harvest_report_required = false,
            harvest_report_state = null,
            rejected = false,
            permit_number = null,
            permit_type = null,
            state_accepted_to_harvest_permit = null,
            deer_hunting_type = null,
            deer_hunting_other_type_description = null,
            feeding_place = null,
            taigaBeanGoose = null,
            grey_seal_hunting_method = null,
            local_images = "[]",
            remote_images = "[]",
            has_new_images = false,
            location_latitude = 1234,
            location_longitude = 1234,
            location_source = "MANUAL",
            location_accuracy = null,
            location_altitude = null,
            location_altitudeAccuracy = null,
            actor_id = null,
            selected_club_id = selectedClubId
        )
        return this
    }

    private fun RiistaDatabase.createRepository(): HuntingClubsSelectedForEntriesRepository {
        return HuntingClubsSelectedForEntriesRepository(
            database = this,
            clubStorage = HuntingClubRepository(this)
        )
    }

    private val username = "user"
}
