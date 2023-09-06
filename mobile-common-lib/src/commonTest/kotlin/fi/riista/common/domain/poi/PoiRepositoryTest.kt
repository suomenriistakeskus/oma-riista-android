package fi.riista.common.domain.poi

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.domain.poi.model.PointOfInterestType
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import kotlin.test.Test
import kotlin.test.assertEquals

class PoiRepositoryTest {

    @Test
    fun testReplace() = runBlockingTest {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = PoiRepository(database)

        // First insert values to the database and check that retrieving it returns same values
        val poiLocation = PoiLocation(
            id = 123L,
            poiId = POI_ID,
            description = "This is description",
            visibleId = 2,
            geoLocation = ETRMSGeoLocation(
                latitude = 1223456,
                longitude = 654321,
                source = GeoLocationSource.MANUAL.toBackendEnum(),
            )
        )
        val poiLocationGroup = PoiLocationGroup(
            id = POI_ID,
            rev = 1,
            visibleId = 2,
            clubId = 22L,
            description = "Group description",
            type = PointOfInterestType.MINERAL_LICK.toBackendEnum(),
            lastModifiedDate = LocalDateTime(2015, 9, 1, 20, 0, 0),
            lastModifierName = "Pekka",
            lastModifierRiistakeskus = true,
            locations = listOf(poiLocation),
        )
        repository.replacePoiLocationGroups(
            externalId = "DZFM5KSKAY",
            poiLocationGroups = listOf(poiLocationGroup),
        )

        val poiLocationGroupsFromRepository = repository.getPoiLocationGroups(EXTERNAL_ID)
        assertEquals(1, poiLocationGroupsFromRepository.size)
        assertEquals(poiLocationGroup, poiLocationGroupsFromRepository[0])

        // Then replace same POI_ID with different values and check that retrieving return those and only those values
        val newPoiLocation = PoiLocation(
            id = 124L,
            poiId = POI_ID,
            description = "This is new description",
            visibleId = 1,
            geoLocation = ETRMSGeoLocation(
                latitude = 888888,
                longitude = 999999,
                source = GeoLocationSource.GPS_DEVICE.toBackendEnum(),
            )
        )
        val newPoiLocationGroup = PoiLocationGroup(
            id = POI_ID,
            rev = 2,
            visibleId = 1,
            clubId = 33L,
            description = "Hew group description",
            type = PointOfInterestType.SIGHTING_PLACE.toBackendEnum(),
            lastModifiedDate = LocalDateTime(2016, 8, 1, 20, 0, 0),
            lastModifierName = "Antti",
            lastModifierRiistakeskus = false,
            locations = listOf(newPoiLocation),
        )
        repository.replacePoiLocationGroups(
            externalId = "DZFM5KSKAY",
            poiLocationGroups = listOf(newPoiLocationGroup),
        )
        val newPoiLocationGroupsFromRepository = repository.getPoiLocationGroups(EXTERNAL_ID)
        assertEquals(1, newPoiLocationGroupsFromRepository.size)
        assertEquals(newPoiLocationGroup, newPoiLocationGroupsFromRepository[0])
    }

    companion object {
        private const val EXTERNAL_ID = "DZFM5KSKAY"
        private const val POI_ID = 101L
    }
}
