package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.CommonSrvaMethod
import fi.riista.common.domain.srva.model.CommonSrvaSpecimen
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.withNumberOfElements
import fi.riista.mobile.models.srva.SrvaApproverInfo
import fi.riista.mobile.models.srva.SrvaAuthorInfo
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.models.srva.SrvaMethod
import fi.riista.mobile.models.srva.SrvaSpecimen

fun SrvaEvent.toCommonSrvaEvent(): CommonSrvaEvent? {
    val localDateTime = pointOfTime?.let {
        LocalDateTime.parseLocalDateTime(it)
    } ?: kotlin.run {
        return null
    }

    val species: Species = when (gameSpeciesCode) {
        null -> Species.Other
        else -> Species.Known(speciesCode = gameSpeciesCode)
    }

    return CommonSrvaEvent(
        localId = localId,
        localUrl = null,
        remoteId = remoteId,
        revision = rev,
        mobileClientRefId = mobileClientRefId,
        srvaSpecVersion = srvaEventSpecVersion,
        state = state.toBackendEnum(),
        rhyId = rhyId,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        location = geoLocation.toETRMSGeoLocation(),
        pointOfTime = localDateTime,
        author = authorInfo?.toCommonSrvaEventAuthor(),
        approver = approverInfo?.toCommonSrvaEventApprover(),
        species = species,
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
        personCount = personCount,
        hoursSpent = timeSpent,
        description = description,
        images = EntityImages(
            // TODO: don't reverse remoteIds if order from the backend changes
            // it seems that latest images added to SRVA event are actually the first in the list
            // --> reverse the collection
            remoteImageIds = imageIds.reversed(),
            localImages = localImages.map { localImage ->
                localImage.toLocalEntityImage(
                    uploaded = imageIds.contains(localImage.serverId)
                )
            }
        ),
    )
}

fun SrvaAuthorInfo.toCommonSrvaEventAuthor() = CommonSrvaEventAuthor(
    id = id,
    revision = rev,
    byName = byName,
    lastName = lastName,
)

fun SrvaApproverInfo.toCommonSrvaEventApprover() = CommonSrvaEventApprover(
    firstName = firstName,
    lastName = lastName,
)

fun SrvaSpecimen.toCommonSrvaSpecimen() = CommonSrvaSpecimen(
    gender = gender.toBackendEnum(),
    age = age.toBackendEnum()
)

fun SrvaMethod.toCommonSrvaMethod() =
    CommonSrvaMethod(
        type = name.toBackendEnum(),
        selected = isChecked,
    )
