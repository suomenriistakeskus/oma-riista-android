package fi.riista.common.domain.harvest

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.database.model.toDbEntityImageString
import fi.riista.common.database.model.toEntityImages
import fi.riista.common.database.util.DbImageUtil
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.keepNonEmpty
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubRepository
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.model.getHuntingYearEnd
import fi.riista.common.domain.model.getHuntingYearStart
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toStringISO8601WithTime
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

internal data class HarvestFilter(
    val ownHarvests: Boolean,
    val huntingYear: HuntingYear?,
    val species: List<Species>?,
    val requireImages: Boolean,
)

internal class HarvestRepository(
    database: RiistaDatabase,
) {
    private val harvestQueries = database.dbHarvestQueries
    private val shooterQueries = database.dbHarvestShooterQueries
    private val clubRepository = HuntingClubRepository(database)

    /**
     * If a harvest is new it is inserted to the database. If it is existing then it is updated.
     * If it was inserted then the returned harvest will contain the assigned localId.
     * Any local images that are marked as deleted are removed.
     */
    suspend fun upsertHarvest(
        username: String,
        harvest: CommonHarvest
    ): CommonHarvest = withContext(DatabaseWriteContext) {
        harvest.selectedClub?.let {
            clubRepository.addOrganizationsIfNotExists(listOf(it))
        }

        return@withContext harvestQueries.transactionWithResult {
            if (harvest.localId != null) {
                updateHarvest(username, harvest)
            } else {
                val localHarvest = getLocalHarvestCorrespondingRemoteHarvest(
                    username = username,
                    remoteHarvest = harvest
                )
                if (localHarvest != null) {
                    val mergedHarvest = mergeHarvest(localHarvest = localHarvest, updatingHarvest = harvest)
                    updateHarvest(username = username, harvest = mergedHarvest)
                } else {
                    insertHarvest(username = username, harvest = harvest)
                }
            }
        }
    }

    private fun getLocalHarvestCorrespondingRemoteHarvest(
        username: String,
        remoteHarvest: CommonHarvest
    ): CommonHarvest? {
        var localHarvest = if (remoteHarvest.id != null) {
            harvestQueries.selectByRemoteId(
                username = username,
                remote_id = remoteHarvest.id
            ).executeAsOneOrNull()?.addActorAndSelectedClubAndConvertToCommonHarvest()
        } else {
            null
        }
        if (localHarvest == null && remoteHarvest.mobileClientRefId != null) {
            localHarvest =
                harvestQueries.selectByMobileClientRefId(
                    username = username,
                    mobile_client_ref_id = remoteHarvest.mobileClientRefId
                ).executeAsOneOrNull()?.addActorAndSelectedClubAndConvertToCommonHarvest()

        }
        return localHarvest
    }

    private fun mergeHarvest(localHarvest: CommonHarvest, updatingHarvest: CommonHarvest): CommonHarvest {
        return CommonHarvest(
            localId = localHarvest.localId,
            localUrl = null,
            id = updatingHarvest.id,
            rev = updatingHarvest.rev,
            species = updatingHarvest.species,
            geoLocation = updatingHarvest.geoLocation,
            pointOfTime = updatingHarvest.pointOfTime,
            description = updatingHarvest.description,
            canEdit = updatingHarvest.canEdit,
            modified = updatingHarvest.modified,
            deleted = updatingHarvest.deleted,
            images = EntityImages(
                remoteImageIds = updatingHarvest.images.remoteImageIds,
                localImages = DbImageUtil.updateLocalImagesFromRemote(
                    existingImages = localHarvest.images,
                    updatingImages = updatingHarvest.images,
                )
            ),
            specimens = updatingHarvest.specimens,
            amount = updatingHarvest.amount,
            harvestSpecVersion = updatingHarvest.harvestSpecVersion,
            harvestReportRequired = updatingHarvest.harvestReportRequired,
            harvestReportState = updatingHarvest.harvestReportState,
            permitNumber = updatingHarvest.permitNumber,
            permitType = updatingHarvest.permitType,
            stateAcceptedToHarvestPermit = updatingHarvest.stateAcceptedToHarvestPermit,
            deerHuntingType = updatingHarvest.deerHuntingType,
            deerHuntingOtherTypeDescription = updatingHarvest.deerHuntingOtherTypeDescription,
            mobileClientRefId = updatingHarvest.mobileClientRefId,
            harvestReportDone = updatingHarvest.harvestReportDone,
            rejected = updatingHarvest.rejected,
            feedingPlace = updatingHarvest.feedingPlace,
            taigaBeanGoose = updatingHarvest.taigaBeanGoose,
            greySealHuntingMethod = updatingHarvest.greySealHuntingMethod,
            actorInfo = updatingHarvest.actorInfo,
            selectedClub = updatingHarvest.selectedClub,
        )
    }

    private fun updateHarvest(username: String, harvest: CommonHarvest): CommonHarvest {
        val localId = requireNotNull(harvest.localId) { "updateHarvest: localId" }

        val actorInfoId = if (harvest.actorInfo is GroupHuntingPerson.Guest) {
            upsertShooter(username = username, shooter = harvest.actorInfo.personInformation).local_id
        } else {
            null
        }
        harvestQueries.updateByLocalId(
            local_id = localId,
            remote_id = harvest.id,
            rev = harvest.rev,
            mobile_client_ref_id = harvest.mobileClientRefId,
            can_edit = harvest.canEdit,
            modified = harvest.modified,
            deleted = harvest.deleted,
            spec_version = harvest.harvestSpecVersion,
            game_species_code = harvest.species.knownSpeciesCodeOrNull(),
            specimens = harvest.specimens.toDbSpecimensString(),
            amount = harvest.amount,
            point_of_time = harvest.pointOfTime.toStringISO8601(),
            description = harvest.description,
            harvest_report_done = harvest.harvestReportDone,
            harvest_report_required = harvest.harvestReportRequired,
            harvest_report_state = harvest.harvestReportState.rawBackendEnumValue,
            rejected = harvest.rejected,
            permit_number = harvest.permitNumber,
            permit_type = harvest.permitType,
            state_accepted_to_harvest_permit = harvest.stateAcceptedToHarvestPermit.rawBackendEnumValue,
            deer_hunting_type = harvest.deerHuntingType.rawBackendEnumValue,
            deer_hunting_other_type_description = harvest.deerHuntingOtherTypeDescription,
            feeding_place = harvest.feedingPlace,
            taigaBeanGoose = harvest.taigaBeanGoose,
            grey_seal_hunting_method = harvest.greySealHuntingMethod.rawBackendEnumValue,
            local_images = harvest.images.localImages.toDbEntityImageString(),
            remote_images = harvest.images.remoteImageIds.toDbRemoteImagesString(),
            has_new_images = harvest.images.localImages.hasNewImages(),
            location_latitude = harvest.geoLocation.latitude,
            location_longitude = harvest.geoLocation.longitude,
            location_source = harvest.geoLocation.source.rawBackendEnumValue,
            location_accuracy = harvest.geoLocation.accuracy,
            location_altitude = harvest.geoLocation.altitude,
            location_altitudeAccuracy = harvest.geoLocation.altitudeAccuracy,
            actor_id = actorInfoId,
            selected_club_id = harvest.selectedClub?.id,
        )

        return harvest.copy(images = harvest.images.withDeletedImagesRemoved())
    }

    private fun insertHarvest(username: String, harvest: CommonHarvest): CommonHarvest {
        val actorInfoId = if (harvest.actorInfo is GroupHuntingPerson.Guest) {
            upsertShooter(username = username, shooter = harvest.actorInfo.personInformation).local_id
        } else {
            null
        }

        harvestQueries.insert(
            username = username,
            remote_id = harvest.id,
            rev = harvest.rev,
            mobile_client_ref_id = harvest.mobileClientRefId,
            can_edit = harvest.canEdit,
            modified = harvest.modified,
            deleted = harvest.deleted,
            spec_version = harvest.harvestSpecVersion,
            game_species_code = harvest.species.knownSpeciesCodeOrNull(),
            specimens = harvest.specimens.toDbSpecimensString(),
            amount = harvest.amount,
            point_of_time = harvest.pointOfTime.toStringISO8601(),
            description = harvest.description,
            harvest_report_done = harvest.harvestReportDone,
            harvest_report_required = harvest.harvestReportRequired,
            harvest_report_state = harvest.harvestReportState.rawBackendEnumValue,
            rejected = harvest.rejected,
            permit_number = harvest.permitNumber,
            permit_type = harvest.permitType,
            state_accepted_to_harvest_permit = harvest.stateAcceptedToHarvestPermit.rawBackendEnumValue,
            deer_hunting_type = harvest.deerHuntingType.rawBackendEnumValue,
            deer_hunting_other_type_description = harvest.deerHuntingOtherTypeDescription,
            feeding_place = harvest.feedingPlace,
            taigaBeanGoose = harvest.taigaBeanGoose,
            grey_seal_hunting_method = harvest.greySealHuntingMethod.rawBackendEnumValue,
            local_images = harvest.images.localImages.toDbEntityImageString(),
            remote_images = harvest.images.remoteImageIds.toDbRemoteImagesString(),
            has_new_images = harvest.images.localImages.hasNewImages(),
            location_latitude = harvest.geoLocation.latitude,
            location_longitude = harvest.geoLocation.longitude,
            location_source = harvest.geoLocation.source.rawBackendEnumValue,
            location_accuracy = harvest.geoLocation.accuracy,
            location_altitude = harvest.geoLocation.altitude,
            location_altitudeAccuracy = harvest.geoLocation.altitudeAccuracy,
            actor_id = actorInfoId,
            selected_club_id = harvest.selectedClub?.id,
        )
        val insertedHarvestId = harvestQueries.lastInsertRowId().executeAsOne()

        return harvest.copy(localId = insertedHarvestId, images = harvest.images.withDeletedImagesRemoved())
    }

    suspend fun getByLocalId(localId: Long): CommonHarvest = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.selectByLocalId(localId)
                .executeAsOne()
                .addActorAndSelectedClubAndConvertToCommonHarvest()
        }
    }

    suspend fun getByRemoteId(
        username: String,
        remoteId: Long,
    ): CommonHarvest? = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.selectByRemoteId(username = username, remote_id = remoteId)
                .executeAsOneOrNull()
                ?.addActorAndSelectedClubAndConvertToCommonHarvest()
        }
    }

    suspend fun listHarvests(username: String): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.selectByUser(username = username)
                .executeAsList()
                .map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    suspend fun getModifiedHarvests(username: String): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.getModifiedHarvests(username = username)
                .executeAsList()
                .map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    suspend fun markDeleted(harvestLocalId: Long?): CommonHarvest? {
        return if (harvestLocalId != null) {
            withContext(DatabaseWriteContext) {
                harvestQueries.transactionWithResult {
                    harvestQueries.markDeleted(harvestLocalId)

                    harvestQueries.selectByLocalId(local_id = harvestLocalId)
                        .executeAsOneOrNull()
                        ?.addActorAndSelectedClubAndConvertToCommonHarvest()
                }
            }
        } else {
            null
        }
    }

    suspend fun hardDelete(harvest: CommonHarvest) {
        if (harvest.localId != null) {
            withContext(DatabaseWriteContext) {
                harvestQueries.hardDelete(harvest.localId)
            }
        }
    }

    suspend fun hardDeleteByRemoteId(username: String, remoteId: Long) = withContext(DatabaseWriteContext) {
        harvestQueries.hardDeleteByRemoteId(username, remoteId)
    }

    suspend fun getDeletedHarvests(username: String): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries
                .getDeletedHarvests(username = username)
                .executeAsList()
                .map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    fun getHarvestHuntingYears(username: String): List<Int> {
        return harvestQueries.getHarvestDateTimes(username)
            .executeAsList()
            .mapNotNull { LocalDateTime.parseLocalDateTime(it)?.date?.getHuntingYear() }
            .distinct()
    }

    suspend fun getHarvestsWithLocalImages(
        username: String,
    ): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            // Get first local_ids and then get corresponding harvests, as it is not possible to get a list of
            // DbHarvest when query contains "local_images IS NOT NULL", because SqlDelight is too clever in that case.
            val localIds = harvestQueries
                .getHarvestIdsWithLocalImages(username = username)
                .executeAsList()

            if (localIds.isEmpty()) {
                return@transactionWithResult emptyList()
            }

            harvestQueries
                .getHarvestsWithLocalIds(local_id = localIds)
                .executeAsList()
                .map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    suspend fun getHarvestsWithImagesNeedingUploading(
        username: String,
    ): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.getHarvestsWithNewImages(username)
                .executeAsList()
                .map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    fun getLatestHarvestSpecies(username: String, size: Int): List<Species> {
        return harvestQueries.getLatestHarvestSpecies(username, size.toLong())
            .executeAsList()
            .mapNotNull {
                when (it.game_species_code) {
                    null -> null
                    else -> Species.Known(it.game_species_code)
                }
            }
    }

    suspend fun filter(
        username: String,
        filter: HarvestFilter,
    ): List<CommonHarvest> = withContext(DatabaseWriteContext) {
        val startDate = filter.huntingYear?.getHuntingYearStart()
        val endDate = filter.huntingYear?.getHuntingYearEnd()
        return@withContext harvestQueries.transactionWithResult {
            harvestQueries.filter(
                username = username,
                startDateTime = startDate.toStringISO8601WithTime(LocalTime.minLocalTime),
                endDateTime = endDate.toStringISO8601WithTime(LocalTime.maxLocalTime),
                filterSpecies = !filter.species.isNullOrEmpty(),
                species = filter.species?.mapNotNull { it.knownSpeciesCodeOrNull() } ?: emptyList(),
                requireImages = filter.requireImages,
                ownHarvests = filter.ownHarvests,
            ).executeAsList().map { it.addActorAndSelectedClubAndConvertToCommonHarvest() }
        }
    }

    private fun upsertShooter(username: String, shooter: PersonWithHunterNumber): DbHarvestShooter {
        if (shooterQueries.exists(username, shooter.id).executeAsOne()) {
            shooterQueries.update(
                rev = shooter.rev,
                byName = shooter.byName,
                lastName = shooter.lastName,
                hunterNumber = shooter.hunterNumber,
                extendedName = shooter.extendedName,
                username = username,
                remote_id = shooter.id,
            )
        } else {
            shooterQueries.insert(
                username = username,
                remote_id = shooter.id,
                rev = shooter.rev,
                byName = shooter.byName,
                lastName = shooter.lastName,
                hunterNumber = shooter.hunterNumber,
                extendedName = shooter.extendedName,
            )
        }
        return shooterQueries
            .getByRemoteId(username = username, remote_id = shooter.id)
            .executeAsOne()
    }

    fun getShooters(username: String): List<PersonWithHunterNumber> {
        return shooterQueries
            .getAll(username)
            .executeAsList()
            .map { it.toPersonWithHunterNumber() }
    }

    /**
     * Remember to wrap previous query (one that provides DbHarvest) in transaction before calling this function
     * function. It is otherwise possible, although not likely, that shooter/club are not correctly added.
     */
    private fun DbHarvest.addActorAndSelectedClubAndConvertToCommonHarvest(): CommonHarvest {
        val actor = if (this.actor_id != null) {
            shooterQueries.getByLocalId(this.actor_id).executeAsOneOrNull()
        } else {
            null
        }

        val selectedClub = selected_club_id?.let {
            clubRepository.findByRemoteId(organizationId = it)
        }
        return this.toCommonHarvest(actor, selectedClub)
    }
}


@Serializable
private data class DbHarvestSpecimen(
    val id: Long?,
    val rev: Int?,
    val gender: String?,
    val age: String?,
    val weight: Double?,
    val weightEstimated: Double?,
    val weightMeasured: Double?,
    val fitnessClass: String?,
    val antlersLost: Boolean?,
    val antlersType: String?,
    val antlersWidth: Int?,
    val antlerPointsLeft: Int?,
    val antlerPointsRight: Int?,
    val antlersGirth: Int?,
    val antlersLength: Int?,
    val antlersInnerWidth: Int?,
    val antlerShaftWidth: Int?,
    val notEdible: Boolean?,
    val alone: Boolean?,
    val additionalInfo: String?,
)

private fun List<EntityImage>.hasNewImages() = this.count { it.status == EntityImage.Status.LOCAL } > 0

private fun List<String>.toDbRemoteImagesString(): String? {
    if (this.isEmpty()) {
        return null
    }
    return this.serializeToJson()
}

private fun DbHarvestSpecimen.toCommonHarvestSpecimen() = CommonHarvestSpecimen(
    id = id,
    rev = rev,
    gender = gender.toBackendEnum(),
    age = age.toBackendEnum(),
    weight = weight,
    weightEstimated = weightEstimated,
    weightMeasured = weightMeasured,
    fitnessClass = fitnessClass.toBackendEnum(),
    antlersLost = antlersLost,
    antlersType = antlersType.toBackendEnum(),
    antlersWidth = antlersWidth,
    antlerPointsLeft = antlerPointsLeft,
    antlerPointsRight = antlerPointsRight,
    antlersGirth = antlersGirth,
    antlersLength = antlersLength,
    antlersInnerWidth = antlersInnerWidth,
    antlerShaftWidth = antlerShaftWidth,
    notEdible = notEdible,
    alone = alone,
    additionalInfo = additionalInfo,
)

private fun CommonHarvestSpecimen.toDbHarvestSpecimen() = DbHarvestSpecimen(
    id = id,
    rev = rev,
    gender = gender.rawBackendEnumValue,
    age = age.rawBackendEnumValue,
    weight = weight,
    weightEstimated = weightEstimated,
    weightMeasured = weightMeasured,
    fitnessClass = fitnessClass.rawBackendEnumValue,
    antlersLost = antlersLost,
    antlersType = antlersType.rawBackendEnumValue,
    antlersWidth = antlersWidth,
    antlerPointsLeft = antlerPointsLeft,
    antlerPointsRight = antlerPointsRight,
    antlersGirth = antlersGirth,
    antlersLength = antlersLength,
    antlersInnerWidth = antlersInnerWidth,
    antlerShaftWidth = antlerShaftWidth,
    notEdible = notEdible,
    alone = alone,
    additionalInfo = additionalInfo,
)

private fun List<CommonHarvestSpecimen>.toDbSpecimensString() =
    this.keepNonEmpty()
        .map { it.toDbHarvestSpecimen() }
        .serializeToJson()

private fun String.toCommonHarvestSpecimens() =
    this.deserializeFromJson<List<DbHarvestSpecimen>>()?.map { it.toCommonHarvestSpecimen() }

private fun DbHarvest.toCommonHarvest(actor: DbHarvestShooter?, selectedClub: Organization?): CommonHarvest {
    val harvest = CommonHarvest(
        localId = local_id,
        localUrl = null,
        id = remote_id,
        rev = rev,
        species = when (game_species_code) {
            null -> Species.Other
            else -> Species.Known(speciesCode = game_species_code)
        },
        geoLocation = ETRMSGeoLocation(
            latitude = location_latitude,
            longitude = location_longitude,
            source = location_source.toBackendEnum(),
            accuracy = location_accuracy,
            altitude = location_altitude,
            altitudeAccuracy = location_altitudeAccuracy,
        ),
        pointOfTime = requireNotNull(LocalDateTime.parseLocalDateTime(point_of_time)) { "CommonHarvest: pointOfTime" },
        description = description,
        canEdit = can_edit,
        modified = modified,
        deleted = deleted,
        images = EntityImages(
            localImages = local_images?.toEntityImages() ?: listOf(),
            remoteImageIds = remote_images?.deserializeFromJson<List<String>>() ?: listOf(),
        ),
        specimens = specimens.toCommonSpecimens(),
        amount = amount,
        harvestSpecVersion = spec_version,
        harvestReportRequired = harvest_report_required,
        harvestReportState = harvest_report_state.toBackendEnum(),
        permitNumber = permit_number,
        permitType = permit_type,
        stateAcceptedToHarvestPermit = state_accepted_to_harvest_permit.toBackendEnum(),
        deerHuntingType = deer_hunting_type.toBackendEnum(),
        deerHuntingOtherTypeDescription = deer_hunting_other_type_description,
        mobileClientRefId = mobile_client_ref_id,
        harvestReportDone = harvest_report_done,
        rejected = rejected,
        feedingPlace = feeding_place,
        taigaBeanGoose = taigaBeanGoose,
        greySealHuntingMethod = grey_seal_hunting_method.toBackendEnum(),
        actorInfo = if (actor != null) {
            GroupHuntingPerson.Guest(actor.toPersonWithHunterNumber())
        } else {
            GroupHuntingPerson.Unknown
        },
        selectedClub = selectedClub,
    )
    return harvest
}

private fun String?.toCommonSpecimens(): List<CommonHarvestSpecimen> {
    return this?.toCommonHarvestSpecimens() ?: emptyList()
}

private fun DbHarvestShooter.toPersonWithHunterNumber() =
    PersonWithHunterNumber(
        id = remote_id,
        rev = rev,
        byName = byName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        extendedName = extendedName,
    )
