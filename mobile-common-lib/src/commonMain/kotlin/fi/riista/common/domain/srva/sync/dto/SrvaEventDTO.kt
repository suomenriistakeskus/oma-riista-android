package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.toPersonWithHunterNumberDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toETRMSGeoLocation
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
data class SrvaEventDTO(
    val id: Long,
    val rev: Long,
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int? = null,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<String>,
    val eventName: String,
    val deportationOrderNumber: String? = null,
    val eventType: String,
    val eventTypeDetail: String? = null,
    val otherEventTypeDetailDescription: String? = null,
    val totalSpecimenAmount: Int,
    val otherMethodDescription: String? = null,
    val otherTypeDescription: String? = null,
    val methods: List<SrvaMethodDTO>,
    val personCount: Int? = 0,
    val timeSpent: Int? = 0,
    val eventResult: String? = null,
    val eventResultDetail: String? = null,
    val authorInfo: PersonWithHunterNumberDTO? = null,
    val specimens: List<SrvaSpecimenDTO>? = null,
    val rhyId: Int? = null,
    val state: String? = null,
    val otherSpeciesDescription: String? = null,
    val approverInfo: SrvaApproverInfoDTO? = null,
    val mobileClientRefId: Long? = null,
    val srvaEventSpecVersion: Int
)

fun SrvaEventDTO.toCommonSrvaEvent(
    localId: Long? = null,
    modified: Boolean = false,
    deleted: Boolean = false,
): CommonSrvaEvent? {
    val pointOfTime = pointOfTime.toLocalDateTime() ?: return null

    return CommonSrvaEvent(
        localId = localId,
        localUrl = null,
        remoteId = id,
        revision = rev,
        mobileClientRefId = mobileClientRefId,
        srvaSpecVersion = srvaEventSpecVersion,
        state = state.toBackendEnum(),
        rhyId = rhyId,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        location = geoLocation.toETRMSGeoLocation(),
        pointOfTime = pointOfTime,
        author = authorInfo?.toCommonSrvaEventAuthor(),
        approver = approverInfo?.toCommonSrvaEventApprover(),
        species = when (gameSpeciesCode) {
            null -> Species.Other
            else -> Species.Known(gameSpeciesCode)
        },
        otherSpeciesDescription = otherSpeciesDescription,
        totalSpecimenAmount = totalSpecimenAmount,
        specimens = specimens?.map { it.toCommonSrvaSpecimen() } ?: listOf(),
        eventCategory = eventName.toBackendEnum(),
        deportationOrderNumber = deportationOrderNumber,
        eventType = eventType.toBackendEnum(),
        otherEventTypeDescription = otherTypeDescription,
        eventTypeDetail = eventTypeDetail.toBackendEnum(),
        otherEventTypeDetailDescription = otherEventTypeDetailDescription,
        eventResult = eventResult.toBackendEnum(),
        eventResultDetail = eventResultDetail.toBackendEnum(),
        methods = methods.map { it.toCommonSrvaMethod() },
        otherMethodDescription = otherMethodDescription,
        personCount = personCount ?: 0,
        hoursSpent = timeSpent ?: 0,
        description = description,
        images = EntityImages(
            remoteImageIds = imageIds,
            localImages = listOf()
        ),
    )
}

fun PersonWithHunterNumberDTO.toCommonSrvaEventAuthor(): CommonSrvaEventAuthor {
    return CommonSrvaEventAuthor(
        id = id,
        revision = rev.toLong(),
        byName = byName,
        lastName = lastName,
    )
}

fun CommonSrvaEvent.toSrvaEventDTO(): SrvaEventDTO? {
    if (remoteId == null || revision == null ||
        eventCategory.rawBackendEnumValue == null || eventType.rawBackendEnumValue == null) {
        return null
    }

    return SrvaEventDTO(
        id = remoteId,
        rev = revision,
        type = "SRVA",
        geoLocation = location.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        gameSpeciesCode = species.knownSpeciesCodeOrNull(),
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        eventName = eventCategory.rawBackendEnumValue,
        deportationOrderNumber = deportationOrderNumber,
        eventType = eventType.rawBackendEnumValue,
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
