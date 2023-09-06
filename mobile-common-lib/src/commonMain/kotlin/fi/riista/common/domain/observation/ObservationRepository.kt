package fi.riista.common.domain.observation

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.database.model.toDbEntityImageString
import fi.riista.common.database.model.toEntityImages
import fi.riista.common.database.util.DbImageUtil.updateLocalImagesFromRemote
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.model.getHuntingYearEnd
import fi.riista.common.domain.model.getHuntingYearStart
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.domain.observation.model.keepNonEmpty
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toStringISO8601WithTime
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

internal data class ObservationFilter(
    val huntingYear: HuntingYear?,
    val species: List<Species>?,
    val requireImages: Boolean,
)

internal class ObservationRepository(database: RiistaDatabase) {
    private val observationQueries = database.dbObservationQueries

    /**
     * If an observation is new it is inserted to the database. If it is existing then it is updated.
     * If it was inserted then the returned observation will contain the assigned localId.
     * Any local images that are marked as deleted are removed.
     */
    suspend fun upsertObservation(
        username: String,
        observation: CommonObservation,
    ): CommonObservation = withContext(DatabaseWriteContext) {
        return@withContext observationQueries.transactionWithResult {
            if (observation.localId != null) {
                updateObservation(observation)
            } else {
                val localObservation = getLocalObservationCorrespondingRemoteObservation(
                    username = username,
                    remoteObservation = observation
                )
                if (localObservation != null) {
                    val mergedEvent = mergeObservation(
                        localObservation = localObservation,
                        updatingObservation = observation
                    )
                    updateObservation(observation = mergedEvent)
                } else {
                    insertObservation(username = username, observation = observation)
                }
            }
        }
    }

    private fun getLocalObservationCorrespondingRemoteObservation(
        username: String,
        remoteObservation: CommonObservation
    ): CommonObservation? {
        var localEvent = if (remoteObservation.remoteId != null) {
            observationQueries.selectByRemoteId(
                username = username,
                remote_id = remoteObservation.remoteId
            ).executeAsOneOrNull()?.toCommonObservation()
        } else {
            null
        }
        if (localEvent == null && remoteObservation.mobileClientRefId != null) {
            localEvent = observationQueries.selectByMobileClientRefId(
                username = username,
                mobile_client_ref_id = remoteObservation.mobileClientRefId
            ).executeAsOneOrNull()?.toCommonObservation()
        }
        return localEvent
    }

    private fun mergeObservation(localObservation: CommonObservation, updatingObservation: CommonObservation): CommonObservation {
        return CommonObservation(
            localId = localObservation.localId,
            localUrl = null,
            remoteId = updatingObservation.remoteId,
            revision = updatingObservation.revision,
            mobileClientRefId = updatingObservation.mobileClientRefId,
            observationSpecVersion = updatingObservation.observationSpecVersion,
            species = updatingObservation.species,
            observationCategory = updatingObservation.observationCategory,
            observationType = updatingObservation.observationType,
            deerHuntingType = updatingObservation.deerHuntingType,
            deerHuntingOtherTypeDescription = updatingObservation.deerHuntingOtherTypeDescription,
            location = updatingObservation.location,
            pointOfTime = updatingObservation.pointOfTime,
            description = updatingObservation.description,
            images = EntityImages(
                remoteImageIds = updatingObservation.images.remoteImageIds,
                localImages = updateLocalImagesFromRemote(
                    existingImages = localObservation.images,
                    updatingImages = updatingObservation.images,
                )
            ),
            specimens = updatingObservation.specimens,
            canEdit = updatingObservation.canEdit,
            modified = updatingObservation.modified,
            deleted = updatingObservation.deleted,
            totalSpecimenAmount = updatingObservation.totalSpecimenAmount,
            mooselikeMaleAmount = updatingObservation.mooselikeMaleAmount,
            mooselikeFemaleAmount = updatingObservation.mooselikeFemaleAmount,
            mooselikeFemale1CalfAmount = updatingObservation.mooselikeFemale1CalfAmount,
            mooselikeFemale2CalfsAmount = updatingObservation.mooselikeFemale2CalfsAmount,
            mooselikeFemale3CalfsAmount = updatingObservation.mooselikeFemale3CalfsAmount,
            mooselikeFemale4CalfsAmount = updatingObservation.mooselikeFemale4CalfsAmount,
            mooselikeCalfAmount = updatingObservation.mooselikeCalfAmount,
            mooselikeUnknownSpecimenAmount = updatingObservation.mooselikeUnknownSpecimenAmount,
            observerName = updatingObservation.observerName,
            observerPhoneNumber = updatingObservation.observerPhoneNumber,
            officialAdditionalInfo = updatingObservation.officialAdditionalInfo,
            verifiedByCarnivoreAuthority = updatingObservation.verifiedByCarnivoreAuthority,
            inYardDistanceToResidence = updatingObservation.inYardDistanceToResidence,
            litter = updatingObservation.litter,
            pack = updatingObservation.pack,
        )
    }

    private fun updateObservation(observation: CommonObservation): CommonObservation {
        val localId = requireNotNull(observation.localId) { "updateObservation: localId" }

        observationQueries.updateByLocalId(
            local_id = localId,
            remote_id = observation.remoteId,
            rev = observation.revision,
            mobile_client_ref_id = observation.mobileClientRefId,
            can_edit = observation.canEdit,
            modified = observation.modified,
            deleted = observation.deleted,
            spec_version = observation.observationSpecVersion,
            game_species_code = observation.species.knownSpeciesCodeOrNull(),
            observation_category = observation.observationCategory.rawBackendEnumValue,
            observation_type = observation.observationType.rawBackendEnumValue,
            deer_hunting_type = observation.deerHuntingType.rawBackendEnumValue,
            deer_hunting_other_type_description = observation.deerHuntingOtherTypeDescription,
            point_of_time = observation.pointOfTime.toStringISO8601(),
            description = observation.description,
            specimens = observation.specimens?.toDbSpecimensString(),
            total_specimen_amount = observation.totalSpecimenAmount,
            mooselike_male_amount = observation.mooselikeMaleAmount,
            mooselike_female_amount = observation.mooselikeFemaleAmount,
            mooselike_female_1calf_amount = observation.mooselikeFemale1CalfAmount,
            mooselike_female_2calfs_amount = observation.mooselikeFemale2CalfsAmount,
            mooselike_female_3calfs_amount = observation.mooselikeFemale3CalfsAmount,
            mooselike_female_4calfs_amount = observation.mooselikeFemale4CalfsAmount,
            mooselike_calf_amount = observation.mooselikeCalfAmount,
            mooselike_unknown_specimen_amount = observation.mooselikeUnknownSpecimenAmount,
            observer_name = observation.observerName,
            observer_phone_number = observation.observerPhoneNumber,
            official_additional_info = observation.officialAdditionalInfo,
            verified_by_carnivore_authority = observation.verifiedByCarnivoreAuthority,
            in_yard_distance_to_residence = observation.inYardDistanceToResidence,
            litter = observation.litter,
            pack = observation.pack,
            local_images = observation.images.localImages.toDbEntityImageString(),
            remote_images = observation.images.remoteImageIds.toDbRemoteImagesString(),
            has_new_images = observation.images.localImages.hasNewImages(),
            location_latitude = observation.location.latitude,
            location_longitude = observation.location.longitude,
            location_source = observation.location.source.rawBackendEnumValue,
            location_accuracy = observation.location.accuracy,
            location_altitude = observation.location.altitude,
            location_altitudeAccuracy = observation.location.altitudeAccuracy,
        )

        return observation.copy(images = observation.images.withDeletedImagesRemoved())
    }

    private fun insertObservation(username: String, observation: CommonObservation): CommonObservation {
        observationQueries.insert(
            username = username,
            remote_id = observation.remoteId,
            rev = observation.revision,
            mobile_client_ref_id = observation.mobileClientRefId,
            spec_version = observation.observationSpecVersion,
            game_species_code = observation.species.knownSpeciesCodeOrNull(),
            observation_category = observation.observationCategory.rawBackendEnumValue,
            observation_type = observation.observationType.rawBackendEnumValue,
            deer_hunting_type = observation.deerHuntingType.rawBackendEnumValue,
            deer_hunting_other_type_description = observation.deerHuntingOtherTypeDescription,
            location_latitude = observation.location.latitude,
            location_longitude = observation.location.longitude,
            location_source = observation.location.source.rawBackendEnumValue,
            location_accuracy = observation.location.accuracy,
            location_altitude = observation.location.altitude,
            location_altitudeAccuracy = observation.location.altitudeAccuracy,
            point_of_time = observation.pointOfTime.toStringISO8601(),
            description = observation.description,
            total_specimen_amount = observation.totalSpecimenAmount,
            specimens = observation.specimens?.toDbSpecimensString(),
            can_edit = observation.canEdit,
            modified = observation.modified,
            deleted = observation.deleted,
            mooselike_male_amount = observation.mooselikeMaleAmount,
            mooselike_female_amount = observation.mooselikeFemaleAmount,
            mooselike_female_1calf_amount = observation.mooselikeFemale1CalfAmount,
            mooselike_female_2calfs_amount = observation.mooselikeFemale2CalfsAmount,
            mooselike_female_3calfs_amount = observation.mooselikeFemale3CalfsAmount,
            mooselike_female_4calfs_amount = observation.mooselikeFemale4CalfsAmount,
            mooselike_calf_amount = observation.mooselikeCalfAmount,
            mooselike_unknown_specimen_amount = observation.mooselikeUnknownSpecimenAmount,
            observer_name = observation.observerName,
            observer_phone_number = observation.observerPhoneNumber,
            official_additional_info = observation.officialAdditionalInfo,
            verified_by_carnivore_authority = observation.verifiedByCarnivoreAuthority,
            in_yard_distance_to_residence = observation.inYardDistanceToResidence,
            litter = observation.litter,
            pack = observation.pack,
            local_images = observation.images.localImages.toDbEntityImageString(),
            remote_images = observation.images.remoteImageIds.toDbRemoteImagesString(),
            has_new_images = observation.images.localImages.hasNewImages(),
        )
        val insertedObservationId = observationQueries.lastInsertRowId().executeAsOne()

        return observation.copy(localId = insertedObservationId, images = observation.images.withDeletedImagesRemoved())
    }

    fun getByLocalId(localId: Long): CommonObservation {
        return observationQueries.selectByLocalId(localId)
            .executeAsOne()
            .toCommonObservation()
    }

    fun getByRemoteId(username: String, remoteId: Long): CommonObservation? {
        return observationQueries.selectByRemoteId(username = username, remote_id = remoteId)
            .executeAsOneOrNull()
            ?.toCommonObservation()
    }

    fun listObservations(username: String): List<CommonObservation> {
        return observationQueries.selectByUser(username = username)
            .executeAsList()
            .map { it.toCommonObservation() }
    }

    fun getModifiedObservations(username: String): List<CommonObservation> {
        return observationQueries.getModifiedObservations(username = username)
            .executeAsList()
            .map { it.toCommonObservation() }
    }

    suspend fun markDeleted(observationLocalId: Long?): CommonObservation? {
        return if (observationLocalId != null) {
            withContext(DatabaseWriteContext) {
                observationQueries.transactionWithResult {
                    observationQueries.markDeleted(observationLocalId)

                    observationQueries.selectByLocalId(local_id = observationLocalId)
                        .executeAsOneOrNull()
                        ?.toCommonObservation()
                }
            }
        } else {
            null
        }
    }

    suspend fun hardDelete(observation: CommonObservation) {
        if (observation.localId != null) {
            withContext(DatabaseWriteContext) {
                observationQueries.hardDelete(observation.localId)
            }
        }
    }

    suspend fun hardDeleteByRemoteId(username: String, remoteId: Long) = withContext(DatabaseWriteContext) {
        observationQueries.hardDeleteByRemoteId(username, remoteId)
    }

    fun getDeletedObservations(username: String): List<CommonObservation> {
        return observationQueries
            .getDeletedObservations(username = username)
            .executeAsList()
            .map { it.toCommonObservation() }
    }

    fun getObservationHuntingYears(username: String): List<Int> {
        return observationQueries.getObservationDateTimes(username)
            .executeAsList()
            .mapNotNull { LocalDateTime.parseLocalDateTime(it)?.date?.getHuntingYear() }
            .distinct()
    }

    suspend fun getObservationsWithLocalImages(
        username: String,
    ): List<CommonObservation> = withContext(DatabaseWriteContext) {
        return@withContext observationQueries.transactionWithResult {
            // Get first local_ids and then get corresponding observations, as it is not possible to get a list of
            // DbObservation when query contains "local_images IS NOT NULL", because SqlDelight is too clever in that case.
            val localIds = observationQueries
                .getObservationIdsWithLocalImages(username = username)
                .executeAsList()

            if (localIds.isEmpty()) {
                return@transactionWithResult emptyList()
            }

            observationQueries
                .getObservationsWithLocalIds(local_id = localIds)
                .executeAsList()
                .map { it.toCommonObservation() }
        }
    }

    fun getObservationsWithImagesNeedingUploading(username: String): List<CommonObservation> {
        return observationQueries.getObservationsWithNewImages(username)
            .executeAsList()
            .map {  it.toCommonObservation() }
    }

    fun getLatestObservationSpecies(username: String, size: Int): List<Species> {
        return observationQueries.getLatestObservationSpecies(username, size.toLong())
            .executeAsList()
            .mapNotNull {
                when (it.game_species_code) {
                    null -> null
                    else -> Species.Known(it.game_species_code)
                }
            }
    }

    fun filter(username: String, filter: ObservationFilter): List<CommonObservation> {
        val startDate = filter.huntingYear?.getHuntingYearStart()
        val endDate = filter.huntingYear?.getHuntingYearEnd()
        return observationQueries.filter(
            username = username,
            startDateTime = startDate?.toStringISO8601WithTime(LocalTime.minLocalTime),
            endDateTime = endDate?.toStringISO8601WithTime(LocalTime.maxLocalTime),
            filterSpecies = !filter.species.isNullOrEmpty(),
            species = filter.species?.mapNotNull { it.knownSpeciesCodeOrNull() } ?: emptyList(),
            requireImages = filter.requireImages,
        ).executeAsList().map { it.toCommonObservation() }
    }
}

@Serializable
private data class DbObservationSpecimen(
    val remoteId: Long?,
    val revision: Int?,
    val gender: String?,
    val age: String?,
    val stateOfHealth: String?,
    val marking: String?,
    val widthOfPaw: Double?,
    val lengthOfPaw: Double?,
)

private fun List<EntityImage>.hasNewImages() = this.count { it.status == EntityImage.Status.LOCAL } > 0

private fun List<String>.toDbRemoteImagesString(): String? {
    if (this.isEmpty()) {
        return null
    }
    return this.serializeToJson()
}

private fun DbObservationSpecimen.toCommonObservationSpecimen() = CommonObservationSpecimen(
    remoteId = remoteId,
    revision = revision,
    gender = gender.toBackendEnum(),
    age = age.toBackendEnum(),
    stateOfHealth = stateOfHealth.toBackendEnum(),
    marking = marking.toBackendEnum(),
    widthOfPaw = widthOfPaw,
    lengthOfPaw = lengthOfPaw,
)

private fun CommonObservationSpecimen.toDbObservationSpecimen() = DbObservationSpecimen(
    remoteId = remoteId,
    revision = revision,
    gender = gender.rawBackendEnumValue,
    age = age.rawBackendEnumValue,
    stateOfHealth = stateOfHealth.rawBackendEnumValue,
    marking = marking.rawBackendEnumValue,
    widthOfPaw = widthOfPaw,
    lengthOfPaw = lengthOfPaw,
)

private fun List<CommonObservationSpecimen>.toDbSpecimensString(): String? {
    return this
        .keepNonEmpty()
        .map { it.toDbObservationSpecimen() }
        .serializeToJson()
}

private fun String.toCommonObservationSpecimens(): List<CommonObservationSpecimen>? {
    return this.deserializeFromJson<List<DbObservationSpecimen>>()?.map { it.toCommonObservationSpecimen() }
}

private fun DbObservation.toCommonObservation(): CommonObservation {
    return CommonObservation(
        localId = local_id,
        localUrl = null,
        remoteId = remote_id,
        revision = rev,
        mobileClientRefId = mobile_client_ref_id,
        observationSpecVersion = spec_version,
        species = when (game_species_code) {
            null -> Species.Other
            else -> Species.Known(speciesCode = game_species_code)
        },
        observationCategory = observation_category.toBackendEnum(),
        observationType = observation_type.toBackendEnum(),
        deerHuntingType = deer_hunting_type.toBackendEnum(),
        deerHuntingOtherTypeDescription = deer_hunting_other_type_description,
        location = ETRMSGeoLocation(
            latitude = location_latitude,
            longitude = location_longitude,
            source = location_source.toBackendEnum(),
            accuracy = location_accuracy,
            altitude = location_altitude,
            altitudeAccuracy = location_altitudeAccuracy,
        ),
        pointOfTime = requireNotNull(LocalDateTime.parseLocalDateTime(point_of_time)) { "CommonObservation: pointOfTime" },
        description = description,
        images = EntityImages(
            localImages = local_images?.toEntityImages() ?: listOf(),
            remoteImageIds = remote_images?.deserializeFromJson<List<String>>() ?: listOf(),
        ),
        totalSpecimenAmount = total_specimen_amount,
        specimens = specimens?.toCommonObservationSpecimens(),
        canEdit = can_edit,
        modified = modified,
        deleted = deleted,
        mooselikeMaleAmount = mooselike_male_amount,
        mooselikeFemaleAmount = mooselike_female_amount,
        mooselikeFemale1CalfAmount = mooselike_female_1calf_amount,
        mooselikeFemale2CalfsAmount = mooselike_female_2calfs_amount,
        mooselikeFemale3CalfsAmount = mooselike_female_3calfs_amount,
        mooselikeFemale4CalfsAmount = mooselike_female_4calfs_amount,
        mooselikeCalfAmount = mooselike_calf_amount,
        mooselikeUnknownSpecimenAmount = mooselike_unknown_specimen_amount,
        observerName = observer_name,
        observerPhoneNumber = observer_phone_number,
        officialAdditionalInfo = official_additional_info,
        verifiedByCarnivoreAuthority = verified_by_carnivore_authority,
        inYardDistanceToResidence = in_yard_distance_to_residence,
        litter = litter,
        pack = pack,
    )
}
