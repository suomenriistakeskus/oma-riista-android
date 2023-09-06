package fi.riista.common.domain.srva

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.database.model.toDbEntityImageString
import fi.riista.common.database.model.toEntityImages
import fi.riista.common.database.util.DbImageUtil.updateLocalImagesFromRemote
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.CommonSrvaMethod
import fi.riista.common.domain.srva.model.CommonSrvaSpecimen
import fi.riista.common.domain.srva.model.keepNonEmpty
import fi.riista.common.logging.getLogger
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toStringISO8601WithTime
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

data class SrvaEventFilter(
    val year: Int?,
    val species: List<Species>?,
    val requireImages: Boolean,
)

internal class SrvaEventRepository(database: RiistaDatabase) {
    private val srvaEventQueries = database.dbSrvaEventQueries

    /**
     * If an event is new it is inserted to the database. If it is existing then it is updated.
     * If it was inserted then the returned event will contain the assigned localId.
     * Any local images that are marked as deleted are removed.
     */
    suspend fun upsertSrvaEvent(
        username: String,
        srvaEvent: CommonSrvaEvent,
    ): CommonSrvaEvent = withContext(DatabaseWriteContext) {
        return@withContext srvaEventQueries.transactionWithResult {
            if (srvaEvent.localId != null) {
                updateSrvaEvent(username = username, srvaEvent = srvaEvent)
            } else {
                val localEvent = getLocalEventCorrespondingRemoteEvent(username = username, remoteEvent = srvaEvent)
                if (localEvent != null) {
                    val mergedEvent = mergeEvent(localEvent = localEvent, updatingEvent = srvaEvent)
                    updateSrvaEvent(username = username, srvaEvent = mergedEvent)
                } else {
                    insertSrvaEvent(username = username, srvaEvent = srvaEvent)
                }
            }
        }
    }

    private fun getLocalEventCorrespondingRemoteEvent(username: String, remoteEvent: CommonSrvaEvent): CommonSrvaEvent? {
        var localEvent = if (remoteEvent.remoteId != null) {
            srvaEventQueries.selectByRemoteId(
                username = username,
                remote_id = remoteEvent.remoteId
            ).executeAsOneOrNull()?.toCommonSrvaEvent()
        } else {
            null
        }
        if (localEvent == null && remoteEvent.mobileClientRefId != null) {
            localEvent = srvaEventQueries.selectByMobileClientRefId(
                username = username,
                mobile_client_ref_id = remoteEvent.mobileClientRefId
            ).executeAsOneOrNull()?.toCommonSrvaEvent()
        }
        return localEvent
    }

    private fun mergeEvent(localEvent: CommonSrvaEvent, updatingEvent: CommonSrvaEvent): CommonSrvaEvent {
        return CommonSrvaEvent(
            localId = localEvent.localId,
            localUrl = null,
            remoteId = updatingEvent.remoteId,
            revision = updatingEvent.revision,
            mobileClientRefId = updatingEvent.mobileClientRefId,
            srvaSpecVersion = updatingEvent.srvaSpecVersion,
            state = updatingEvent.state,
            rhyId = updatingEvent.rhyId,
            canEdit = updatingEvent.canEdit,
            modified = updatingEvent.modified,
            deleted = updatingEvent.deleted,
            location = updatingEvent.location,
            pointOfTime = updatingEvent.pointOfTime,
            author = updatingEvent.author,
            approver = updatingEvent.approver,
            species = updatingEvent.species,
            otherSpeciesDescription = updatingEvent.otherSpeciesDescription,
            totalSpecimenAmount = updatingEvent.totalSpecimenAmount,
            specimens = updatingEvent.specimens,
            eventCategory = updatingEvent.eventCategory,
            deportationOrderNumber = updatingEvent.deportationOrderNumber,
            eventType = updatingEvent.eventType,
            otherEventTypeDescription = updatingEvent.otherEventTypeDescription,
            eventTypeDetail = updatingEvent.eventTypeDetail,
            otherEventTypeDetailDescription = updatingEvent.otherEventTypeDetailDescription,
            eventResult = updatingEvent.eventResult,
            eventResultDetail = updatingEvent.eventResultDetail,
            methods = updatingEvent.methods,
            otherMethodDescription = updatingEvent.otherMethodDescription,
            personCount = updatingEvent.personCount,
            hoursSpent = updatingEvent.hoursSpent,
            description = updatingEvent.description,
            images = EntityImages(
                remoteImageIds = updatingEvent.images.remoteImageIds,
                localImages = updateLocalImagesFromRemote(
                    existingImages = localEvent.images,
                    updatingImages = updatingEvent.images,
                )
            )
        )
    }

    private fun updateSrvaEvent(username: String, srvaEvent: CommonSrvaEvent): CommonSrvaEvent {
        val localId = requireNotNull(srvaEvent.localId) { "updateFromLocal: localId" }

        if (srvaEvent.remoteId != null) {
            // This is needed to fix a case that could happen on first published version of the new SRVA sync.
            val existingEvent = srvaEventQueries.selectByRemoteId(
                username = username,
                remote_id = srvaEvent.remoteId
            ).executeAsOneOrNull()
            if (existingEvent != null && existingEvent.local_id != localId) {
                // Event we are updating already exists in the database with different localId?
                // This situation can happen if a response to CreateSrvaEvent is not received but
                // backend has created a new event, and that is then received as a new event when
                // loading events from backend. -> Delete the duplicate.
                logger.i { "Deleting a duplicate SRVA event remoteId=${srvaEvent.remoteId}" }
                srvaEventQueries.hardDelete(existingEvent.local_id)
            }
        }

        srvaEventQueries.updateByLocalId(
            remote_id = srvaEvent.remoteId,
            rev = srvaEvent.revision,
            mobile_client_ref_id = srvaEvent.mobileClientRefId,
            spec_version = srvaEvent.srvaSpecVersion,
            state = srvaEvent.state.rawBackendEnumValue,
            rhy_id = srvaEvent.rhyId,
            can_edit = srvaEvent.canEdit,
            modified = srvaEvent.modified,
            deleted = srvaEvent.deleted,
            point_of_time = srvaEvent.pointOfTime.toStringISO8601(),
            author_id = srvaEvent.author?.id,
            author_rev = srvaEvent.author?.revision,
            author_by_name = srvaEvent.author?.byName,
            author_last_name = srvaEvent.author?.lastName,
            approver_first_name = srvaEvent.approver?.firstName,
            approver_last_name = srvaEvent.approver?.lastName,
            game_species_code = srvaEvent.species.knownSpeciesCodeOrNull(),
            other_species_description = srvaEvent.otherSpeciesDescription,
            total_specimen_amount = srvaEvent.totalSpecimenAmount,
            specimens = srvaEvent.specimens.toDbSpecimensString(),
            event_category = srvaEvent.eventCategory.rawBackendEnumValue,
            deportation_order_number = srvaEvent.deportationOrderNumber,
            event_type = srvaEvent.eventType.rawBackendEnumValue,
            other_event_type_description = srvaEvent.otherEventTypeDescription,
            event_type_detail = srvaEvent.eventTypeDetail.rawBackendEnumValue,
            other_event_type_detail_description = srvaEvent.otherEventTypeDetailDescription,
            event_result = srvaEvent.eventResult.rawBackendEnumValue,
            event_result_detail = srvaEvent.eventResultDetail.rawBackendEnumValue,
            methods = srvaEvent.methods.toDbMethodString(),
            other_method_description = srvaEvent.otherMethodDescription,
            person_count = srvaEvent.personCount,
            hours_spent = srvaEvent.hoursSpent,
            description = srvaEvent.description,
            location_latitude = srvaEvent.location.latitude,
            location_longitude = srvaEvent.location.longitude,
            location_source = srvaEvent.location.source.rawBackendEnumValue,
            location_accuracy = srvaEvent.location.accuracy,
            location_altitude = srvaEvent.location.altitude,
            location_altitudeAccuracy = srvaEvent.location.altitudeAccuracy,
            local_images = srvaEvent.images.localImages.toDbEntityImageString(),
            remote_images = getRemoteImagesAsString(srvaEvent),
            local_id = localId,
        )

        return srvaEvent.copy(images = srvaEvent.images.withDeletedImagesRemoved())
    }

    private fun insertSrvaEvent(username: String, srvaEvent: CommonSrvaEvent): CommonSrvaEvent {
        srvaEventQueries.insert(
            username = username,
            remote_id = srvaEvent.remoteId,
            rev = srvaEvent.revision,
            mobile_client_ref_id = srvaEvent.mobileClientRefId,
            spec_version = srvaEvent.srvaSpecVersion,
            state = srvaEvent.state.rawBackendEnumValue,
            rhy_id = srvaEvent.rhyId,
            can_edit = srvaEvent.canEdit,
            modified = srvaEvent.modified,
            deleted = srvaEvent.deleted,
            point_of_time = srvaEvent.pointOfTime.toStringISO8601(),
            author_id = srvaEvent.author?.id,
            author_rev = srvaEvent.author?.revision,
            author_by_name = srvaEvent.author?.byName,
            author_last_name = srvaEvent.author?.lastName,
            approver_first_name = srvaEvent.approver?.firstName,
            approver_last_name = srvaEvent.approver?.lastName,
            game_species_code = srvaEvent.species.knownSpeciesCodeOrNull(),
            other_species_description = srvaEvent.otherSpeciesDescription,
            total_specimen_amount = srvaEvent.totalSpecimenAmount,
            specimens = srvaEvent.specimens.toDbSpecimensString(),
            event_category = srvaEvent.eventCategory.rawBackendEnumValue,
            deportation_order_number = srvaEvent.deportationOrderNumber,
            event_type = srvaEvent.eventType.rawBackendEnumValue,
            other_event_type_description = srvaEvent.otherEventTypeDescription,
            event_type_detail = srvaEvent.eventTypeDetail.rawBackendEnumValue,
            other_event_type_detail_description = srvaEvent.otherEventTypeDetailDescription,
            event_result = srvaEvent.eventResult.rawBackendEnumValue,
            event_result_detail = srvaEvent.eventResultDetail.rawBackendEnumValue,
            methods = srvaEvent.methods.toDbMethodString(),
            other_method_description = srvaEvent.otherMethodDescription,
            person_count = srvaEvent.personCount,
            hours_spent = srvaEvent.hoursSpent,
            description = srvaEvent.description,
            location_latitude = srvaEvent.location.latitude,
            location_longitude = srvaEvent.location.longitude,
            location_source = srvaEvent.location.source.rawBackendEnumValue,
            location_accuracy = srvaEvent.location.accuracy,
            location_altitude = srvaEvent.location.altitude,
            location_altitudeAccuracy = srvaEvent.location.altitudeAccuracy,
            local_images = srvaEvent.images.localImages.toDbEntityImageString(),
            remote_images = getRemoteImagesAsString(srvaEvent),
        )
        val insertedEventId = srvaEventQueries.lastInsertRowId().executeAsOne()

        return srvaEvent.copy(localId = insertedEventId, images = srvaEvent.images.withDeletedImagesRemoved())
    }

    fun getByLocalId(localId: Long): CommonSrvaEvent {
        return srvaEventQueries.selectByLocalId(localId)
            .executeAsOne()
            .toCommonSrvaEvent()
    }

    fun getByRemoteId(username: String, remoteId: Long): CommonSrvaEvent? {
        return srvaEventQueries.selectByRemoteId(username = username, remote_id = remoteId)
            .executeAsOneOrNull()
            ?.toCommonSrvaEvent()
    }

    fun listEvents(username: String): List<CommonSrvaEvent> {
        return srvaEventQueries.selectByUser(username = username)
            .executeAsList()
            .map { dbEvent -> dbEvent.toCommonSrvaEvent() }
    }

    fun getModifiedEvents(username: String): List<CommonSrvaEvent> {
        return srvaEventQueries.getModifiedEvents(username = username)
            .executeAsList()
            .map { dbEvent -> dbEvent.toCommonSrvaEvent() }
    }

    suspend fun markDeleted(srvaEventLocalId: Long?): CommonSrvaEvent? {
        return if (srvaEventLocalId != null) {
            withContext(DatabaseWriteContext) {
                srvaEventQueries.transactionWithResult {
                    srvaEventQueries.markDeleted(srvaEventLocalId)

                    srvaEventQueries.selectByLocalId(local_id = srvaEventLocalId)
                        .executeAsOneOrNull()
                        ?.toCommonSrvaEvent()
                }
            }
        } else {
            null
        }
    }

    suspend fun hardDelete(srvaEvent: CommonSrvaEvent) {
        if (srvaEvent.localId != null) {
            withContext(DatabaseWriteContext) {
                srvaEventQueries.hardDelete(srvaEvent.localId)
            }
        }
    }

    suspend fun hardDeleteByRemoteId(username: String, remoteId: Long) = withContext(DatabaseWriteContext) {
        srvaEventQueries.hardDeleteByRemoteId(username, remoteId)
    }

    fun getDeletedEvents(username: String): List<CommonSrvaEvent> {
        return srvaEventQueries.getDeletedEvents(username = username)
            .executeAsList()
            .map { dbEvent -> dbEvent.toCommonSrvaEvent() }
    }

    suspend fun getEventsWithLocalImages(
        username: String,
    ): List<CommonSrvaEvent> = withContext(DatabaseWriteContext) {
        return@withContext srvaEventQueries.transactionWithResult {
            // Get first local_ids and then get corresponding events, as it is not possible to get a list of DbSrvaEvent
            // when query contains "local_images IS NOT NULL", because SqlDelight is too clever in that case.
            val localIds = srvaEventQueries
                .getEventIdsWithLocalImages(username = username)
                .executeAsList()

            if (localIds.isEmpty()) {
                return@transactionWithResult listOf()
            }

            srvaEventQueries
                .getEventsWithLocalIds(local_id = localIds)
                .executeAsList()
                .map { dbEvent -> dbEvent.toCommonSrvaEvent() }
        }
    }

    fun getSrvaYears(username: String): List<Int> {
        return srvaEventQueries.getEventDateTimes(username)
            .executeAsList()
            .mapNotNull { dtString -> LocalDateTime.parseLocalDateTime(dtString) }
            .map { dt -> dt.year }
            .distinct()
    }

    private fun getRemoteImagesAsString(event: CommonSrvaEvent): String? {
        if (event.images.remoteImageIds.isEmpty()) {
            return null
        }
        return event.images.remoteImageIds.serializeToJson()
    }

    fun filter(username: String, filter: SrvaEventFilter): List<CommonSrvaEvent> {
        val startDate = filter.year?.toYearStart()
        val endDate = filter.year?.toYearEnd()
        val filterSpeciesAndOtherSpecies = (filter.species?.contains(Species.Other) ?: false && (filter.species?.size ?: 0) > 1)
        val filterOtherSpecies = !filterSpeciesAndOtherSpecies && (filter.species?.contains(Species.Other) ?: false)
        val filterSpecies = !filterSpeciesAndOtherSpecies && (!filter.species?.filter { it != Species.Other }.isNullOrEmpty())
        val species = filter.species?.mapNotNull { it.knownSpeciesCodeOrNull() } ?: emptyList()
        return srvaEventQueries.filter(
            username = username,
            startDateTime = startDate?.toStringISO8601WithTime(LocalTime.minLocalTime),
            endDateTime = endDate?.toStringISO8601WithTime(LocalTime.maxLocalTime),
            filterSpecies = filterSpecies,
            filterOtherSpecies = filterOtherSpecies,
            filterSpeciesAndOtherSpecies = filterSpeciesAndOtherSpecies,
            species = species,
            requireImages = filter.requireImages,
        ).executeAsList().map { it.toCommonSrvaEvent() }
    }

    companion object {
        private val logger by getLogger(SrvaEventRepository::class)
    }
}

@Serializable
private data class DbSpecimen(
    val gender: String?,
    val age: String?,
)

private fun DbSpecimen.toCommonSrvaSpecimen() = CommonSrvaSpecimen(
    gender = gender.toBackendEnum(),
    age = age.toBackendEnum(),
)

private fun CommonSrvaSpecimen.toDbSpecimen() = DbSpecimen(
    gender = gender.rawBackendEnumValue,
    age = age.rawBackendEnumValue,
)

private fun List<CommonSrvaSpecimen>.toDbSpecimensString() =
    this.keepNonEmpty()
        .map { it.toDbSpecimen() }
        .serializeToJson()

private fun String.toCommonSpecimens(): List<CommonSrvaSpecimen>? {
    return this.deserializeFromJson<List<DbSpecimen>>()?.map { it.toCommonSrvaSpecimen() }
}

@Serializable
private data class DbMethod(
    val type: String?,
    val selected: Boolean
)

private fun DbMethod.toCommonSrvaMethod() = CommonSrvaMethod(
    type = type.toBackendEnum(),
    selected = selected,
)

private fun CommonSrvaMethod.toDbMethod() = DbMethod(
    type = type.rawBackendEnumValue,
    selected = selected,
)

private fun List<CommonSrvaMethod>.toDbMethodString(): String? {
    return this.map { it.toDbMethod() }
        .serializeToJson()
}

private fun String.toCommonSrvaMethods(): List<CommonSrvaMethod>? {
    return this.deserializeFromJson<List<DbMethod>>()?.map { it.toCommonSrvaMethod() }
}

internal fun DbSrvaEvent.toCommonSrvaEvent(): CommonSrvaEvent {
    val specimensOrEmptyList = specimens?.toCommonSpecimens() ?: listOf()

    return CommonSrvaEvent(
        localId = local_id,
        localUrl = null,
        remoteId = remote_id,
        revision = rev,
        mobileClientRefId = mobile_client_ref_id,
        srvaSpecVersion = spec_version,
        state = state.toBackendEnum(),
        rhyId = rhy_id,
        canEdit = can_edit,
        modified = modified,
        deleted = deleted,
        location = ETRMSGeoLocation(
            latitude = location_latitude,
            longitude = location_longitude,
            source = location_source.toBackendEnum(),
            accuracy = location_accuracy,
            altitude = location_altitude,
            altitudeAccuracy = location_altitudeAccuracy,
        ),
        pointOfTime = requireNotNull(LocalDateTime.parseLocalDateTime(point_of_time)) { "toCommonEvent: pointOfTime" },
        author = createAuthor(author_id, author_rev, author_by_name, author_last_name),
        approver = createApprover(approver_first_name, approver_last_name),
        species = when (game_species_code) {
            null -> Species.Other
            else -> Species.Known(speciesCode = game_species_code)
        },
        otherSpeciesDescription = other_species_description,
        // in first database versions there was no column for total_specimen_amount. It is therefore
        // likely that there are events without amount information. In that case all specimens were
        // saved and thus we can here fallback to the size of the specimens list
        totalSpecimenAmount = total_specimen_amount ?: specimensOrEmptyList.size,
        specimens = specimensOrEmptyList,
        eventCategory = event_category.toBackendEnum(),
        deportationOrderNumber = deportation_order_number,
        eventType = event_type.toBackendEnum(),
        otherEventTypeDescription = other_event_type_description,
        eventTypeDetail = event_type_detail.toBackendEnum(),
        otherEventTypeDetailDescription = other_event_type_detail_description,
        eventResult = event_result.toBackendEnum(),
        eventResultDetail = event_result_detail.toBackendEnum(),
        methods = methods?.toCommonSrvaMethods() ?: listOf(),
        otherMethodDescription = other_method_description,
        personCount = person_count,
        hoursSpent = hours_spent,
        description = description,
        images = EntityImages(
            localImages = local_images?.toEntityImages() ?: listOf(),
            remoteImageIds = remote_images?.deserializeFromJson<List<String>>() ?: listOf(),
        )
    )
}

private fun createAuthor(authorId: Long?, revision: Long?, byName: String?, lastName: String?): CommonSrvaEventAuthor? {
    if (authorId != null && revision != null) {
        return CommonSrvaEventAuthor(
            id = authorId,
            revision = revision,
            byName = byName,
            lastName = lastName
        )
    }
    return null
}

private fun createApprover(firstName: String?, lastName: String?): CommonSrvaEventApprover? {
    if (firstName != null || lastName != null) {
        return CommonSrvaEventApprover(
            firstName = firstName,
            lastName = lastName,
        )
    }
    return null
}

private fun Int.toYearStart() = LocalDate(this, 1, 1)
private fun Int.toYearEnd() = LocalDate(this, 12, 31)
