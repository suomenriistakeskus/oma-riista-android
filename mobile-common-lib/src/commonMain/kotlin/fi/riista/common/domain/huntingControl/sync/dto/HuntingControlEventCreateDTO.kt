package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.dto.AttachmentDTO
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.huntingControl.model.HuntingControlEventStatus
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.HoursAndMinutesDTO
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.model.toETRMSGeoLocationDTO
import fi.riista.common.model.toHoursAndMinutesString
import fi.riista.common.model.toLocalDateDTO
import kotlinx.serialization.Serializable

@Serializable
data class HuntingControlEventCreateDTO(
    val specVersion: Int,
    val mobileClientRefId: Long,
    val eventType: String,
    val status: String,
    val inspectors: List<InspectorDTO>,
    val cooperationTypes: List<String>,
    val wolfTerritory: Boolean,
    val otherParticipants: String? = null,
    val geoLocation: ETRMSGeoLocationDTO,
    val locationDescription: String? = null,
    val date: LocalDateDTO,
    val beginTime: HoursAndMinutesDTO,
    val endTime: HoursAndMinutesDTO,
    val customers: Int,
    val proofOrders: Int,
    val description: String? = null,
    val attachments: List<AttachmentDTO>? = null,
)

fun HuntingControlEvent.toHuntingControlEventCreateDTO(): HuntingControlEventCreateDTO? {

    if (mobileClientRefId == null || eventType.rawBackendEnumValue == null) {
        return null
    }

    return HuntingControlEventCreateDTO(
        specVersion = specVersion,
        mobileClientRefId = mobileClientRefId,
        eventType = eventType.rawBackendEnumValue,
        status = status.rawBackendEnumValue ?: HuntingControlEventStatus.PROPOSED.rawBackendEnumValue,
        inspectors = inspectors.map { it.toInspectorDTO() },
        cooperationTypes = cooperationTypes.mapNotNull { type -> type.rawBackendEnumValue },
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
    )
}
