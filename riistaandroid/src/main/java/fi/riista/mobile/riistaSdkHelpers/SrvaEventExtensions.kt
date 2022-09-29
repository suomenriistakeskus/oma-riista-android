package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.*
import fi.riista.common.domain.srva.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.withNumberOfElements
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.LocalImage
import fi.riista.mobile.models.srva.*

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

    val commonSpecimens =
        (specimens?.map { it.toCommonSrvaSpecimen() } ?: listOf())
            .withNumberOfElements(totalSpecimenAmount) {
                CommonSrvaSpecimen(
                    gender = BackendEnum.create(null),
                    age = BackendEnum.create(null),
                )
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
        location = geoLocation.toETRMSGeoLocation(),
        pointOfTime = localDateTime,
        author = authorInfo?.toCommonSrvaEventAuthor(),
        approver = approverInfo?.toCommonSrvaEventApprover(),
        species = species,
        otherSpeciesDescription = otherSpeciesDescription,
        specimens = commonSpecimens,
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

fun CommonSrvaEventAuthor.toSrvaAuthorInfo(): SrvaAuthorInfo {
    return SrvaAuthorInfo().also { authorInfo ->
        authorInfo.id = id
        authorInfo.rev = revision
        authorInfo.byName = byName
        authorInfo.lastName = lastName
    }
}

fun SrvaApproverInfo.toCommonSrvaEventApprover() = CommonSrvaEventApprover(
    firstName = firstName,
    lastName = lastName,
)

fun CommonSrvaEventApprover.toSrvaApproverInfo(): SrvaApproverInfo {
    return SrvaApproverInfo().also { approverInfo ->
        approverInfo.firstName = firstName
        approverInfo.lastName = lastName
    }
}

fun SrvaSpecimen.toCommonSrvaSpecimen() = CommonSrvaSpecimen(
    gender = gender.toBackendEnum(),
    age = age.toBackendEnum()
)

fun CommonSrvaSpecimen.toSrvaSpecimen(): SrvaSpecimen {
    return SrvaSpecimen().also { specimen ->
        specimen.gender = gender.rawBackendEnumValue
        specimen.age = age.rawBackendEnumValue
    }
}

fun SrvaMethod.toCommonSrvaMethod() =
    CommonSrvaMethod(
        type = name.toBackendEnum(),
        selected = isChecked,
    )

fun CommonSrvaMethod.toSrvaMethod(): SrvaMethod {
    return SrvaMethod().also { method ->
        method.name = type.rawBackendEnumValue
        method.isChecked = selected
    }
}

fun CommonSrvaEvent.toAppSrvaEvent(
    eventDeleted: Boolean = false,
    eventModified: Boolean = false,
): SrvaEvent {
    return SrvaEvent().also { event ->
        event.remoteId = remoteId
        event.rev = revision
        event.type = GameLog.TYPE_SRVA
        event.geoLocation = location.toGeoLocation()
        event.pointOfTime = pointOfTime.toStringISO8601()
        event.gameSpeciesCode = when (val species = species) {
            is Species.Known -> species.speciesCode
            Species.Other,
            Species.Unknown -> null
        }
        event.description = description
        event.canEdit = canEdit
        event.imageIds = images.remoteImageIds
        event.eventName = eventCategory.rawBackendEnumValue
        event.deportationOrderNumber = deportationOrderNumber
        event.eventType = eventType.rawBackendEnumValue
        event.eventTypeDetail = eventTypeDetail.rawBackendEnumValue
        event.otherEventTypeDetailDescription = otherEventTypeDetailDescription
        event.totalSpecimenAmount = specimens.count()
        event.otherMethodDescription = otherMethodDescription
        event.otherTypeDescription = otherEventTypeDescription
        event.methods = methods.map { it.toSrvaMethod() }
        event.personCount = personCount
        event.timeSpent = hoursSpent
        event.eventResult = eventResult.rawBackendEnumValue
        event.eventResultDetail = eventResultDetail.rawBackendEnumValue
        event.authorInfo = author?.toSrvaAuthorInfo()
        event.specimens = specimens.map { it.toSrvaSpecimen() }
        event.rhyId = rhyId
        event.state = state.rawBackendEnumValue
        event.otherSpeciesDescription = otherSpeciesDescription
        event.approverInfo = approver?.toSrvaApproverInfo()
        event.mobileClientRefId = mobileClientRefId
        event.srvaEventSpecVersion = srvaSpecVersion

        // ignored fields in SrvaEvent
        event.localId = localId
        event.deleted = eventDeleted
        event.modified = eventModified
        event.localImages = images.localImages.mapNotNull { entityImage ->
            if (entityImage.localUrl == null) {
                // local images on android are using files i.e. urls and not identifiers
                return@mapNotNull null
            }

            when (entityImage.status) {
                EntityImage.Status.LOCAL,
                EntityImage.Status.UPLOADED ->
                    LocalImage().also { localImage ->
                        localImage.serverId = entityImage.serverId
                        localImage.localPath = entityImage.localUrl
                    }
                EntityImage.Status.LOCAL_TO_BE_REMOVED ->
                    // ignore local images that are marked for removal
                    // -> they will get removed later
                    null
            }
        }
        // username should be applied when/if saving the event
    }
}
