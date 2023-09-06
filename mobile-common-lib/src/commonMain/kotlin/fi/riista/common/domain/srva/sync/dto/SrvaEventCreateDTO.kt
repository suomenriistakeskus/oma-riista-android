package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.toPersonWithHunterNumberDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
data class SrvaEventCreateDTO(
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int?,
    val description: String?,
    val canEdit: Boolean,
    val imageIds: List<String>,
    val eventName: String,
    val deportationOrderNumber: String?,
    val eventType: String,
    val eventTypeDetail: String?,
    val otherEventTypeDetailDescription: String?,
    val totalSpecimenAmount: Int,
    val otherMethodDescription: String?,
    val otherTypeDescription: String?,
    val methods: List<SrvaMethodDTO>,
    val personCount: Int?,
    val timeSpent: Int?,
    val eventResult: String?,
    val eventResultDetail: String?,
    val authorInfo: PersonWithHunterNumberDTO?,
    val specimens: List<SrvaSpecimenDTO>?,
    val rhyId: Int?,
    val state: String?,
    val otherSpeciesDescription: String?,
    val approverInfo: SrvaApproverInfoDTO?,
    val mobileClientRefId: Long?,
    val srvaEventSpecVersion: Int
)

fun CommonSrvaEvent.toSrvaEventCreateDTO(): SrvaEventCreateDTO? {
    val eventName = eventCategory.rawBackendEnumValue ?: return null
    val eventType = eventType.rawBackendEnumValue ?: return null

    return SrvaEventCreateDTO(
        type = "SRVA",
        geoLocation = location.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        gameSpeciesCode = species.knownSpeciesCodeOrNull(),
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        eventName = eventName,
        deportationOrderNumber = deportationOrderNumber,
        eventType = eventType,
        eventTypeDetail = eventTypeDetail.rawBackendEnumValue,
        otherEventTypeDetailDescription = otherEventTypeDetailDescription,
        totalSpecimenAmount = totalSpecimenAmount,
        otherMethodDescription = otherMethodDescription,
        otherTypeDescription = otherEventTypeDescription,
        methods = methods.map { it.toSrvaMethodDTO() },
        personCount = personCount,
        timeSpent = hoursSpent,
        eventResult = eventResult.rawBackendEnumValue,
        eventResultDetail = eventResultDetail.rawBackendEnumValue,
        authorInfo = author?.toPersonWithHunterNumberDTO(),
        specimens = specimens.map { it.toSrvaSpecimenDTO() },
        rhyId = rhyId,
        state = state.rawBackendEnumValue,
        otherSpeciesDescription = otherSpeciesDescription,
        approverInfo = approver?.toSrvaApproverInfoDTO(),
        mobileClientRefId = mobileClientRefId,
        srvaEventSpecVersion = srvaSpecVersion,
    )
}
