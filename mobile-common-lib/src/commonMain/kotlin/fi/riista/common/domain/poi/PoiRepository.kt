package fi.riista.common.domain.poi

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum

internal class PoiRepository(database: RiistaDatabase) {

    private val poiLocationGroupQueries = database.dbPoiLocationGroupQueries
    private val poiLocationQueries = database.dbPoiLocationQueries

    fun getPoiLocationGroups(externalId: String): List<PoiLocationGroup> {
        val dbPoiLocationGroups: List<DbPoiLocationGroup> = poiLocationGroupQueries.selectByExternalId(externalId).executeAsList()
        val poiLocationGroups = dbPoiLocationGroups.map { dbPoiLocationGroup ->
            val dbPoiLocations: List<DbPoiLocation> = poiLocationQueries.selectByPoiId(dbPoiLocationGroup.id).executeAsList()
            val poiLocations = dbPoiLocations.map { dbPoiLocation ->
                dbPoiLocation.toPoiLocation()
            }
            dbPoiLocationGroup.toPoiLocationGroup(poiLocations)
        }
        return poiLocationGroups
    }

    fun replacePoiLocationGroups(externalId: String, poiLocationGroups: List<PoiLocationGroup>) {
        poiLocationGroupQueries.transaction {
            poiLocationGroupQueries.deleteByExternalId(externalId)

            poiLocationGroups.forEach { poiLocationGroup ->
                insertPoiLocationGroup(externalId, poiLocationGroup)
            }
        }
    }

    private fun insertPoiLocationGroup(externalId: String, poiLocationGroup: PoiLocationGroup) {
        poiLocationGroupQueries.insert(
            id = poiLocationGroup.id,
            external_id = externalId,
            rev = poiLocationGroup.rev,
            visible_id = poiLocationGroup.visibleId,
            club_id = poiLocationGroup.clubId,
            description = poiLocationGroup.description,
            type = poiLocationGroup.type.rawBackendEnumValue ?: "",
            last_modified_date = poiLocationGroup.lastModifiedDate?.toStringISO8601(),
            last_modifier_name = poiLocationGroup.lastModifierName,
            last_modifier_riistakeskus = poiLocationGroup.lastModifierRiistakeskus,
        )

        poiLocationGroup.locations.forEach { poiLocation ->
            poiLocationQueries.insert(
                id = poiLocation.id,
                poi_id = poiLocation.poiId,
                external_id = externalId,
                description = poiLocation.description,
                visible_id = poiLocation.visibleId,
                latitude = poiLocation.geoLocation.latitude,
                longitude = poiLocation.geoLocation.longitude,
                source = poiLocation.geoLocation.source.rawBackendEnumValue ?: "",
                accuracy = poiLocation.geoLocation.accuracy,
                altitude = poiLocation.geoLocation.altitude,
                altitudeAccuracy = poiLocation.geoLocation.altitudeAccuracy,
            )
        }
    }
}

fun DbPoiLocationGroup.toPoiLocationGroup(poiLocations: List<PoiLocation>): PoiLocationGroup {
    return PoiLocationGroup(
        id = id,
        rev = rev,
        visibleId = visible_id,
        clubId = club_id,
        description = description,
        type = type.toBackendEnum(),
        lastModifiedDate = last_modified_date?.let {
            LocalDateTime.parseLocalDateTime(last_modified_date)
        },
        lastModifierName = last_modifier_name,
        lastModifierRiistakeskus = last_modifier_riistakeskus,
        locations = poiLocations,
    )
}

fun DbPoiLocation.toPoiLocation(): PoiLocation {
    return PoiLocation(
        id = id,
        poiId = poi_id,
        description = description,
        visibleId = visible_id,
        geoLocation = ETRMSGeoLocation(
            latitude = latitude,
            longitude = longitude,
            source = source.toBackendEnum()
        ),
    )
}
