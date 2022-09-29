package fi.riista.common.domain.huntingControl.model

import fi.riista.common.domain.dto.toHuntingControlAttachment
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.toHuntingControlEventInspector
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.dto.toETRMSGeoLocation
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalTime
import fi.riista.common.model.*
import kotlinx.serialization.Serializable

@Serializable
data class HuntingControlEventData(
    val localId: Long? = null,
    val remoteId: HuntingControlEventId? = null,
    val specVersion: Int,
    val rev: Revision? = null,
    val mobileClientRefId: Long?,
    val rhyId: OrganizationId,
    val eventType: BackendEnum<HuntingControlEventType>,
    val status: BackendEnum<HuntingControlEventStatus>,
    val inspectors: List<HuntingControlEventInspector>,
    val cooperationTypes: List<BackendEnum<HuntingControlCooperationType>>,
    val otherParticipants: String?,
    val location: CommonLocation,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val wolfTerritory: Boolean?,
    val description: String?,
    val locationDescription: String?,
    val proofOrderCount: Int?,
    val customerCount: Int?,
    val canEdit: Boolean,
    val modified: Boolean,
    val attachments: List<HuntingControlAttachment>,
)

fun HuntingControlEvent.toHuntingControlEventData(): HuntingControlEventData {
    return HuntingControlEventData(
        localId = localId,
        remoteId = remoteId,
        specVersion = specVersion,
        rev = rev,
        mobileClientRefId = mobileClientRefId,
        rhyId = rhyId,
        eventType = eventType,
        status = status,
        inspectors = inspectors,
        cooperationTypes = cooperationTypes,
        otherParticipants = otherParticipants,
        location = geoLocation.asKnownLocation(),
        date = date,
        startTime = startTime,
        endTime = endTime,
        wolfTerritory = wolfTerritory,
        description = description,
        locationDescription = locationDescription,
        proofOrderCount = proofOrderCount,
        customerCount = customerCount,
        canEdit = canEdit,
        modified = modified,
        attachments = attachments,
    )
}

fun HuntingControlEventDTO.toHuntingControlEventData(
    localId: Long,
    rhyId: OrganizationId,
    modified: Boolean,
): HuntingControlEventData? {
    val date = date.toLocalDate()
    val beginTime = beginTime.toLocalTime()
    val endTime = endTime.toLocalTime()

    if (date == null || beginTime == null || endTime == null) {
        return null
    }

    return HuntingControlEventData(
        localId = localId,
        specVersion = specVersion,
        remoteId = id,
        rev = rev,
        mobileClientRefId = mobileClientRefId,
        rhyId = rhyId,
        eventType = eventType.toBackendEnum(),
        status = status.toBackendEnum(),
        inspectors = inspectors.map { inspector -> inspector.toHuntingControlEventInspector() },
        cooperationTypes = cooperationTypes.map { type -> type.toBackendEnum<HuntingControlCooperationType>() },
        wolfTerritory = wolfTerritory,
        otherParticipants = otherParticipants,
        location = geoLocation.toETRMSGeoLocation().asKnownLocation(),
        locationDescription = locationDescription,
        date = date,
        startTime = beginTime,
        endTime = endTime,
        customerCount = customers,
        proofOrderCount = proofOrders,
        description = description,
        canEdit = canEdit ?: false,
        modified = modified,
        attachments = attachments.map { it.toHuntingControlAttachment() },
    )
}
