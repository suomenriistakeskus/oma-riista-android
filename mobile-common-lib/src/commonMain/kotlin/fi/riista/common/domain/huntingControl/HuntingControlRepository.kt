package fi.riista.common.domain.huntingControl

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlEventInspector
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.sync.model.GameWarden
import fi.riista.common.domain.huntingControl.sync.model.LoadHuntingControlEvent
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.dto.toLocalDate
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalTime
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.isWithinPeriod
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toHoursAndMinutesString
import fi.riista.common.model.toPeriodDate
import fi.riista.common.util.contains
import kotlinx.coroutines.withContext

internal class HuntingControlRepository(database: RiistaDatabase) {

    private val huntingControlEventQueries = database.dbHuntingControlEventQueries
    private val rhyQueries = database.dbHuntingControlRhyQueries
    private val inspectorQueries = database.dbHuntingControlEventInspectorQueries
    private val gameWardenQueries = database.dbHuntingControlGameWardenQueries
    private val attachmentQueries = database.dbHuntingControlEventAttachmentQueries

    fun hasRhys(username: String): Boolean {
        return rhyQueries.hasRhys(username).executeAsOne()
    }

    fun getRhys(username: String): List<Organization> {
        return rhyQueries.selectByUser(username = username)
            .executeAsList().map { rhy -> rhy.toOrganization() }
    }

    fun getRhy(username: String, rhyId: OrganizationId): Organization? {
        return rhyQueries.getByRemoteId(username = username, remote_id = rhyId)
            .executeAsOneOrNull()
            ?.toOrganization()
    }

    suspend fun getHuntingControlEvents(
        username: String,
        rhyId: OrganizationId,
    ): List<HuntingControlEvent> = withContext(DatabaseWriteContext) {
        return@withContext huntingControlEventQueries.transactionWithResult {
            val dbHuntingControlEvents = huntingControlEventQueries.selectByRhy(
                rhy_id = rhyId,
                username = username,
            ).executeAsList()
            val huntingControlEvents = dbHuntingControlEvents.map { dbEvent ->
                toHuntingControlEventWithInspectorsAndAttachments(dbEvent)
            }
            huntingControlEvents
        }
    }

    suspend fun getHuntingControlEventByLocalId(
        localId: Long,
    ): HuntingControlEvent? = withContext(DatabaseWriteContext) {
        return@withContext huntingControlEventQueries.transactionWithResult {
            huntingControlEventQueries.selectByLocalId(localId)
                .executeAsOneOrNull()
                ?.let { toHuntingControlEventWithInspectorsAndAttachments(it) }
        }
    }

    private fun toHuntingControlEventWithInspectorsAndAttachments(event: DbHuntingControlEvent): HuntingControlEvent {
        val inspectors =
            inspectorQueries.selectByHuntingControlEvent(
                hunting_control_event_id = event.local_id
            ).executeAsList().map { inspector ->
                inspector.toHuntingControlEventInspector()
            }
        val attachments =
            attachmentQueries.selectByEvent(
                event_local_id = event.local_id,
            ).executeAsList().map { attachment ->
                attachment.toHuntingControlAttachment()
            }
        return event.toHuntingControlEvent(inspectors, attachments)
    }

    fun getGameWardens(username: String, rhyId: OrganizationId): List<HuntingControlGameWarden> {
        return gameWardenQueries.selectByUserAndRhy(rhy_id = rhyId, username = username)
            .executeAsList().map { dbGameWarden ->
                dbGameWarden.toHuntingControlGameWarden()
            }
    }

    suspend fun getModifiedHuntingControlEvents(
        username: String,
    ): List<HuntingControlEvent> = withContext(DatabaseWriteContext) {
        return@withContext huntingControlEventQueries.transactionWithResult {
            val dbHuntingControlEvents = huntingControlEventQueries.selectModifiedEventsByUsername(
                username = username,
            ).executeAsList()
            val huntingControlEvents = dbHuntingControlEvents.map { dbEvent ->
                val inspectors = inspectorQueries.selectByHuntingControlEvent(
                    hunting_control_event_id = dbEvent.local_id
                ).executeAsList().map { inspector ->
                    inspector.toHuntingControlEventInspector()
                }
                val attachments = attachmentQueries.selectByEvent(dbEvent.local_id).executeAsList()
                    .map { dbAttachment ->
                        dbAttachment.toHuntingControlAttachment()
                    }
                dbEvent.toHuntingControlEvent(inspectors, attachments)
            }
            huntingControlEvents
        }
    }

    suspend fun createHuntingControlEvent(
        username: String,
        event: HuntingControlEventData,
    ): HuntingControlEvent = withContext(DatabaseWriteContext) {
        return@withContext huntingControlEventQueries.transactionWithResult {
            val eventType = requireNotNull(event.eventType.rawBackendEnumValue)
            val status = requireNotNull(event.status.rawBackendEnumValue)
            val startTime = requireNotNull(event.startTime?.toHoursAndMinutesString())
            val endTime = requireNotNull(event.endTime?.toHoursAndMinutesString())
            val proofOrders = requireNotNull(event.proofOrderCount)
            val customerCount = requireNotNull(event.customerCount)
            val wolfTerritory = requireNotNull(event.wolfTerritory)
            val geoLocation = requireNotNull((event.location as? CommonLocation.Known)?.etrsLocation)

            huntingControlEventQueries.insert(
                remote_id = event.remoteId,
                spec_version = event.specVersion,
                rev = event.rev,
                mobile_client_ref_id = event.mobileClientRefId,
                username = username,
                rhy_id = event.rhyId,
                event_type = eventType,
                status = status,
                cooperation_types = event.cooperationTypes.toDbCooperationTypes(),
                date = event.date.toStringISO8601(),
                start_time = startTime,
                end_time = endTime,
                wolf_territory = wolfTerritory,
                description = event.description,
                location_description = event.locationDescription,
                proof_order_count = proofOrders,
                customer_count = customerCount,
                other_participants = event.otherParticipants,
                can_edit = event.canEdit,
                modified = event.modified,
                latitude = geoLocation.latitude,
                longitude = geoLocation.longitude,
                source = geoLocation.source.rawBackendEnumValue ?: "",
                accuracy = geoLocation.accuracy,
                altitude = geoLocation.altitude,
                altitudeAccuracy = geoLocation.altitudeAccuracy,
            )
            val insertedEventId = huntingControlEventQueries.lastInsertRowId().executeAsOne()
            insertInspectors(event, insertedEventId)
            insertAttachments(event.attachments, insertedEventId)

            val attachments = attachmentQueries.selectByEvent(insertedEventId).executeAsList()
                .map { dbAttachment ->
                    dbAttachment.toHuntingControlAttachment()
                }
            huntingControlEventQueries
                .selectByLocalId(insertedEventId)
                .executeAsOne()
                .toHuntingControlEvent(event.inspectors, attachments)
        }
    }

    suspend fun updateHuntingControlEvent(
        event: HuntingControlEventData,
    ): HuntingControlEvent = withContext(DatabaseWriteContext) {
        val eventType = requireNotNull(event.eventType.rawBackendEnumValue)
        val status = requireNotNull(event.status.rawBackendEnumValue)
        val cooperationTypes = event.cooperationTypes.joinToString(separator = ",") { it.rawBackendEnumValue ?: "" }
        val startTime = requireNotNull(event.startTime?.toHoursAndMinutesString())
        val endTime = requireNotNull(event.endTime?.toHoursAndMinutesString())
        val proofOrders = requireNotNull(event.proofOrderCount)
        val customerCount = requireNotNull(event.customerCount)
        val localId = requireNotNull(event.localId)
        val wolfTerritory = requireNotNull(event.wolfTerritory)
        val geoLocation = requireNotNull((event.location as? CommonLocation.Known)?.etrsLocation)

        return@withContext huntingControlEventQueries.transactionWithResult {
            huntingControlEventQueries.updateByLocalId(
                spec_version = event.specVersion,
                remote_id = event.remoteId,
                rev = event.rev,
                event_type = eventType,
                status = status,
                cooperation_types = cooperationTypes,
                date = event.date.toStringISO8601(),
                start_time = startTime,
                end_time = endTime,
                wolf_territory = wolfTerritory,
                description = event.description,
                location_description = event.locationDescription,
                proof_order_count = proofOrders,
                customer_count = customerCount,
                other_participants = event.otherParticipants,
                can_edit = event.canEdit,
                modified = event.modified,
                latitude = geoLocation.latitude,
                longitude = geoLocation.longitude,
                source = geoLocation.source.rawBackendEnumValue ?: "",
                accuracy = geoLocation.accuracy,
                altitude = geoLocation.altitude,
                altitudeAccuracy = geoLocation.altitudeAccuracy,
                local_id = localId,
            )
            inspectorQueries.deleteByEvent(hunting_control_event_id = localId)
            insertInspectors(event, localId)
            updateAttachments(event.attachments, localId)

            val attachments = attachmentQueries.selectByEvent(localId).executeAsList().map { dbAttachment ->
                dbAttachment.toHuntingControlAttachment()
            }
            huntingControlEventQueries
                .selectByLocalId(localId)
                .executeAsOne()
                .toHuntingControlEvent(event.inspectors, attachments)
        }
    }

    /**
     * Returns a list of remoteIds
     */
    fun listAttachmentsMissingThumbnails(): List<Long> {
        return attachmentQueries.listAttachmentsMissingThumbnail().executeAsList()
    }

    suspend fun setThumbnail(thumbnail: String, attachmentRemoteId: Long) = withContext(DatabaseWriteContext) {
        attachmentQueries.setThumbnail(thumbnail, attachmentRemoteId)
    }

    private fun updateAttachments(attachments: List<HuntingControlAttachment>, eventLocalId: Long) {
        val idsInDb = attachmentQueries.localAndremoteIdsByEvent(eventLocalId).executeAsList()

        // First remove attachments that have been deleted on remote
        idsInDb.forEach { attachmentIds ->
            if (!attachments.contains { it.remoteId == attachmentIds.remote_id }) {
                attachmentQueries.deleteByLocalId(attachmentIds.local_id)
            }
        }
        // Then remove locally deleted attachments
        attachments
            .filter { attachment -> attachment.deleted }
            .forEach { attachment ->
                if (attachment.localId != null) {
                    if (attachment.remoteId == null) {
                        // Not yet sent to remote -> just delete
                        attachmentQueries.deleteByLocalId(attachment.localId)
                    } else {
                        // Already in remote -> mark as deleted
                        attachmentQueries.markDeletedByLocalId(attachment.localId)
                    }
                }
            }

        // Then add new ones
        attachments
            .filter { attachment -> !attachment.deleted }
            .filter { attachment -> attachment.localId == null }
            .forEach { attachment ->
            if (attachment.remoteId == null || !idsInDb.contains { it.remote_id == attachment.remoteId }) {
                attachmentQueries.insert(
                    event_local_id = eventLocalId,
                    remote_id = attachment.remoteId,
                    filename = attachment.fileName,
                    is_image = attachment.isImage,
                    thumbnail = attachment.thumbnailBase64,
                    deleted = attachment.deleted,
                    uuid = attachment.uuid,
                    mime_type = attachment.mimeType,
                )
            }
        }
    }

    private fun insertAttachments(attachments: List<HuntingControlAttachment>, eventLocalId: Long) {
        attachments.forEach { attachment ->
            if (attachment.deleted && attachment.remoteId == null) {
                // Deleted attachment not yet sent to backend. Just drop it.
            } else {
                attachmentQueries.insert(
                    event_local_id = eventLocalId,
                    remote_id = attachment.remoteId,
                    filename = attachment.fileName,
                    is_image = attachment.isImage,
                    thumbnail = attachment.thumbnailBase64,
                    deleted = attachment.deleted,
                    uuid = attachment.uuid,
                    mime_type = attachment.mimeType,
                )
            }
        }
    }

    suspend fun updateGameWardens(
        username: String,
        gameWardens: Map<OrganizationId, List<GameWarden>>,
    ) = withContext(DatabaseWriteContext) {
        gameWardenQueries.transaction {
            gameWardenQueries.deleteByUser(username)

            gameWardens.forEach { entry ->
                val rhyId = entry.key
                val rhyGameWardens = entry.value

                rhyGameWardens.forEach { gameWarden ->
                    gameWardenQueries.insert(
                        rhy_id = rhyId,
                        username = username,
                        remote_id = gameWarden.inspector.id,
                        first_name = gameWarden.inspector.firstName,
                        last_name = gameWarden.inspector.lastName,
                        start_date = gameWarden.beginDate?.toStringISO8601(),
                        end_date = gameWarden.endDate?.toStringISO8601(),
                    )
                }
            }
        }

        // Verify that modified events still have valid inspectors
        gameWardens.forEach { entry ->
            val rhyId = entry.key
            val rhyGameWardensFromDb = gameWardenQueries.selectByUserAndRhy(
                username = username,
                rhy_id = rhyId,
            ).executeAsList()
            val modifiedEvents = huntingControlEventQueries.selectModifiedEventsByUsernameAndRhy(
                username = username,
                rhy_id = rhyId,
            ).executeAsList()
            modifiedEvents.forEach { event ->
                val inspectors = inspectorQueries.selectByHuntingControlEvent(event.local_id).executeAsList()
                val dateTime = LocalDate.parseLocalDate(event.date)
                if (dateTime != null) {
                    inspectors.forEach { inspector ->
                        val dbGameWarden = rhyGameWardensFromDb.firstOrNull { dbWarden ->
                            val warden = dbWarden.toHuntingControlGameWarden()
                            val period = LocalDatePeriod(warden.startDate.toPeriodDate(), warden.endDate.toPeriodDate())
                            warden.remoteId == inspector.remote_id && dateTime.isWithinPeriod(period)
                        }
                        if (dbGameWarden == null) {
                            // An event has an invalid inspector -> remove it
                            inspectorQueries.deleteInspectorFromEvent(
                                remote_id = inspector.remote_id,
                                hunting_control_event_id = event.local_id,
                            )
                        } else if (dbGameWarden.first_name != inspector.first_name
                            || dbGameWarden.last_name != inspector.last_name
                        ) {
                            // Name of the inspector has changed -> update it
                            inspectorQueries.updateInspectorName(
                                first_name = dbGameWarden.first_name,
                                last_name = dbGameWarden.last_name,
                                remote_id = inspector.remote_id,
                                hunting_control_event_id = event.local_id,
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun updateHuntingControlEvents(
        username: String,
        events: Map<OrganizationId, List<LoadHuntingControlEvent>>,
    ) = withContext(DatabaseWriteContext) {
        huntingControlEventQueries.transaction {
            events.forEach { entry ->
                val rhyId = entry.key
                val rhyEvents = entry.value

                rhyEvents.forEach { event ->
                    val eventType = event.eventType.rawBackendEnumValue
                    val status = event.status.rawBackendEnumValue
                    val startTime = requireNotNull(event.beginTime.toHoursAndMinutesString())
                    val endTime = requireNotNull(event.endTime.toHoursAndMinutesString())
                    if (eventType != null && status != null) {
                        if (huntingControlEventQueries.eventExists(event.id, username).executeAsOne()) {
                            val dbRev = huntingControlEventQueries.getEventRevision(event.id, username).executeAsOne().rev
                            if (dbRev == null || event.rev > dbRev) {
                                huntingControlEventQueries.updateByRemoteId(
                                    spec_version = event.specVersion,
                                    rev = event.rev,
                                    event_type = eventType,
                                    status = status,
                                    cooperation_types = event.cooperationTypes.toDbCooperationTypes(),
                                    date = event.date.toStringISO8601(),
                                    start_time = startTime,
                                    end_time = endTime,
                                    wolf_territory = event.wolfTerritory,
                                    description = event.description,
                                    location_description = event.locationDescription,
                                    proof_order_count = event.proofOrders,
                                    customer_count = event.customers,
                                    other_participants = event.otherParticipants,
                                    can_edit = event.canEdit,
                                    modified = false,
                                    latitude = event.geoLocation.latitude,
                                    longitude = event.geoLocation.longitude,
                                    source = event.geoLocation.source.rawBackendEnumValue ?: "",
                                    accuracy = event.geoLocation.accuracy,
                                    altitude = event.geoLocation.altitude,
                                    altitudeAccuracy = event.geoLocation.altitudeAccuracy,
                                    remote_id = event.id,
                                )
                            }
                        } else {
                            huntingControlEventQueries.insert(
                                remote_id = event.id,
                                spec_version = event.specVersion,
                                rev = event.rev,
                                mobile_client_ref_id = event.mobileClientRefId,
                                username = username,
                                rhy_id = rhyId,
                                event_type = eventType,
                                status = status,
                                cooperation_types = event.cooperationTypes.toDbCooperationTypes(),
                                date = event.date.toStringISO8601(),
                                start_time = startTime,
                                end_time = endTime,
                                wolf_territory = event.wolfTerritory,
                                description = event.description,
                                location_description = event.locationDescription,
                                proof_order_count = event.proofOrders,
                                customer_count = event.customers,
                                other_participants = event.otherParticipants,
                                can_edit = event.canEdit,
                                modified = false,
                                latitude = event.geoLocation.latitude,
                                longitude = event.geoLocation.longitude,
                                source = event.geoLocation.source.rawBackendEnumValue ?: "",
                                accuracy = event.geoLocation.accuracy,
                                altitude = event.geoLocation.altitude,
                                altitudeAccuracy = event.geoLocation.altitudeAccuracy,
                            )
                        }
                    } else {
                        logger.w { "Unable to insert hunting control event to DB with ID ${event.id}" }
                    }
                }
            }

            // Update inspectors and attachments by deleting old ones and inserting new ones
            events.forEach { entry ->
                val rhyEvents = entry.value
                rhyEvents.forEach { event ->
                    val eventLocalId = huntingControlEventQueries.getLocalIdByUsernameAndRemoteId(
                        username = username,
                        remote_id = event.id,
                    ).executeAsOne()
                    inspectorQueries.deleteByEvent(eventLocalId)
                    event.inspectors.forEach { inspector ->
                        inspectorQueries.insert(
                            hunting_control_event_id = eventLocalId,
                            remote_id = inspector.id,
                            first_name = inspector.firstName,
                            last_name = inspector.lastName,
                        )
                    }
                    updateAttachments(event.attachments, eventLocalId)
                }
            }
        }
    }

    suspend fun updateRhys(
        username: String,
        rhys: List<Organization>,
    ) = withContext(DatabaseWriteContext) {
        rhyQueries.transaction {
            // First delete from DB RHYs that user is no longer hunting controller for
            val rhysOnDb = getRhys(username)
            rhysOnDb.forEach { rhy ->
                if (!rhys.contains { it.id == rhy.id }) {
                    deleteRhy(username, rhy.id)
                }
            }

            // Then add/update rest
            rhys.forEach { rhy ->
                upsertRhy(
                    username = username,
                    rhy = rhy,
                )
            }
        }
    }

    suspend fun deleteAttachment(attachmentLocalId: Long) = withContext(DatabaseWriteContext) {
        attachmentQueries.deleteByLocalId(attachmentLocalId)
    }

    suspend fun updateAttachmentRemoteId(
        remoteId: Long,
        localId: Long,
    ) = withContext(DatabaseWriteContext) {
        attachmentQueries.updateRemoteIdByLocalId(remote_id = remoteId, local_id = localId)
    }

    fun listAllAttachmentUuids(): List<String> {
        return attachmentQueries.listAllUuids().executeAsList()
    }

    private fun deleteRhy(username: String, id: OrganizationId) {
        rhyQueries.deleteRhy(username = username, remote_id = id)
    }

    private fun upsertRhy(username: String, rhy: Organization) {
        if (rhyQueries.rhyExists(username, rhy.id).executeAsOne()) {
            rhyQueries.updateRhy(
                username = username,
                name_fi = rhy.name.fi,
                name_sv = rhy.name.sv,
                name_en = rhy.name.en,
                official_code = rhy.officialCode,
                remote_id = rhy.id,
            )
        } else {
            rhyQueries.insertRhy(
                username = username,
                remote_id = rhy.id,
                name_fi = rhy.name.fi,
                name_sv = rhy.name.sv,
                name_en = rhy.name.en,
                official_code = rhy.officialCode,
            )
        }
    }

    private fun insertInspectors(event: HuntingControlEventData, localId: Long) {
        event.inspectors.forEach { inspector ->
            inspectorQueries.insert(
                hunting_control_event_id = localId,
                remote_id = inspector.id,
                first_name = inspector.firstName,
                last_name = inspector.lastName,
            )
        }
    }

    companion object {
        private val logger by getLogger(HuntingControlRepository::class)
    }
}

internal fun DbHuntingControlRhy.toOrganization(): Organization {
    return Organization(
        id = remote_id,
        name = LocalizedString(
            fi = name_fi,
            sv = name_sv,
            en = name_en,
        ),
        officialCode = official_code,
    )
}

internal fun DbHuntingControlEvent.toHuntingControlEvent(
    inspectors: List<HuntingControlEventInspector>,
    attachments: List<HuntingControlAttachment>,
): HuntingControlEvent {
    return HuntingControlEvent(
        localId = local_id,
        remoteId = remote_id,
        specVersion = spec_version,
        rev = rev,
        mobileClientRefId = mobile_client_ref_id,
        rhyId = rhy_id,
        eventType = event_type.toBackendEnum(),
        status = status.toBackendEnum(),
        inspectors = inspectors,
        cooperationTypes = cooperation_types.split(",").map { type -> type.toBackendEnum() },
        date = requireNotNull(LocalDate.parseLocalDate(date)),
        otherParticipants = other_participants,
        geoLocation = ETRMSGeoLocation(
            latitude = latitude,
            longitude = longitude,
            source = source.toBackendEnum(),
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
        ),
        startTime = requireNotNull(LocalTime.parseLocalTime(start_time)),
        endTime = requireNotNull(LocalTime.parseLocalTime(end_time)),
        wolfTerritory = wolf_territory,
        description = description,
        locationDescription = location_description,
        proofOrderCount = proof_order_count,
        customerCount = customer_count,
        canEdit = can_edit,
        modified = modified,
        attachments = attachments,
    )
}

internal fun DbHuntingControlEventInspector.toHuntingControlEventInspector(): HuntingControlEventInspector {
    return HuntingControlEventInspector(
        id = remote_id,
        firstName = first_name,
        lastName = last_name,
    )
}

internal fun DbHuntingControlGameWarden.toHuntingControlGameWarden(): HuntingControlGameWarden {
    return HuntingControlGameWarden(
        remoteId = remote_id,
        firstName = first_name,
        lastName = last_name,
        startDate = start_date?.toLocalDate(),
        endDate = end_date?.toLocalDate(),
    )
}

internal fun List<BackendEnum<HuntingControlCooperationType>>.toDbCooperationTypes(): String {
    return this.joinToString(separator = ",") { it.rawBackendEnumValue ?: "" }
}

internal fun DbHuntingControlEventAttachment.toHuntingControlAttachment(): HuntingControlAttachment {
    return HuntingControlAttachment(
        localId = local_id,
        remoteId = remote_id,
        fileName = filename,
        isImage = is_image,
        thumbnailBase64 = thumbnail,
        deleted = deleted,
        uuid = uuid,
        mimeType = mime_type,
    )
}
