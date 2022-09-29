package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.dto.AttachmentDTO
import fi.riista.common.domain.dto.toHuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.toAttachmentDTO
import fi.riista.common.domain.huntingControl.sync.model.LoadHuntingControlEvent
import fi.riista.common.dto.*
import fi.riista.common.logging.Logger
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toETRMSGeoLocationDTO
import fi.riista.common.model.toHoursAndMinutesString
import fi.riista.common.model.toLocalDateDTO
import kotlinx.serialization.Serializable

@Serializable
data class HuntingControlEventDTO(
    val specVersion: Int,
    val id: Long,
    val rev: Int,
    val mobileClientRefId: Long? = null,
    val eventType: String,
    val status: String,
    val inspectors: List<InspectorDTO>,
    val cooperationTypes: List<String>,
    val wolfTerritory: Boolean,
    val otherParticipants: String? = null,
    val geoLocation: ETRMSGeoLocationDTO,
    val locationDescription: String? = null,
    val date: LocalDateDTO,
    val beginTime: LocalTimeDTO,
    val endTime: LocalTimeDTO,
    val customers: Int,
    val proofOrders: Int,
    val description: String? = null,
    val attachments: List<AttachmentDTO>,
    val canEdit: Boolean? = null,
)

fun HuntingControlEventDTO.toLoadHuntingControlEvent(logger: Logger): LoadHuntingControlEvent? {
    val localDate = date.toLocalDate()
    val beginLocalTime = beginTime.toLocalTime()
    val endLocalTime = endTime.toLocalTime()

    if (localDate == null || beginLocalTime == null || endLocalTime == null) {
        logger.d { "Unable to create LoadHuntingControlEvent (id=$id). date=$date, beginTime=$beginTime, endTime=$endTime" }
        return null
    }

    return LoadHuntingControlEvent(
        specVersion = specVersion,
        id = id,
        rev = rev,
        mobileClientRefId = mobileClientRefId,
        eventType = eventType.toBackendEnum(),
        status = status.toBackendEnum(),
        inspectors = inspectors.map { inspector -> inspector.toHuntingControlEventInspector() },
        cooperationTypes = cooperationTypes.map { type -> type.toBackendEnum<HuntingControlCooperationType>() },
        wolfTerritory = wolfTerritory,
        otherParticipants = otherParticipants,
        geoLocation = geoLocation.toETRMSGeoLocation(),
        locationDescription = locationDescription,
        date = localDate,
        beginTime = beginLocalTime,
        endTime = endLocalTime,
        customers = customers,
        proofOrders = proofOrders,
        description = description,
        attachments = attachments.map { attachment -> attachment.toHuntingControlAttachment() },
        canEdit = canEdit ?: false,
    )
}

fun HuntingControlEvent.toHuntingControlEventDTO(): HuntingControlEventDTO? {
    val remoteId = remoteId ?: return null
    val revision = rev ?: return null
    val eventType = eventType.rawBackendEnumValue ?: ""

    return HuntingControlEventDTO(
        specVersion = specVersion,
        id = remoteId,
        rev = revision,
        mobileClientRefId = mobileClientRefId,
        eventType = eventType,
        status = status.rawBackendEnumValue ?: "",
        inspectors = inspectors.map { inspector -> inspector.toInspectorDTO() },
        cooperationTypes = cooperationTypes.map { ctype -> ctype.rawBackendEnumValue ?: "" },
        wolfTerritory = wolfTerritory,
        otherParticipants = otherParticipants,
        geoLocation = geoLocation.toETRMSGeoLocationDTO(),
        locationDescription = locationDescription,
        date = date.toLocalDateDTO(),
        beginTime = startTime.toHoursAndMinutesString(),
        endTime = endTime.toHoursAndMinutesString(),
        customers = customerCount,
        proofOrders = proofOrderCount,
        description = description,
        attachments = attachments.mapNotNull { attachment -> attachment.toAttachmentDTO() },
        canEdit = canEdit,
    )

}
