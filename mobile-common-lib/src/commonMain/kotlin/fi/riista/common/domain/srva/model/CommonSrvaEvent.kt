package fi.riista.common.domain.srva.model

import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaEvent(
    val localId: Long?,
    val localUrl: String?, // Used as localId on iOS
    val remoteId: Long?,
    val revision: Long?,
    val mobileClientRefId: Long?,
    val srvaSpecVersion: Int,
    val state: BackendEnum<SrvaEventState>,
    val rhyId: Int?,

    val canEdit: Boolean,
    val modified: Boolean,
    val deleted: Boolean,

    val location: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val author: CommonSrvaEventAuthor?,
    val approver: CommonSrvaEventApprover?,

    val species: Species,
    val otherSpeciesDescription: String?,
    val totalSpecimenAmount: Int,
    val specimens: List<CommonSrvaSpecimen>,

    val eventCategory: BackendEnum<SrvaEventCategoryType>,
    val deportationOrderNumber: String?,
    val eventType: BackendEnum<SrvaEventType>,
    val otherEventTypeDescription: String?,
    val eventTypeDetail: BackendEnum<SrvaEventTypeDetail>,
    val otherEventTypeDetailDescription: String?,
    val eventResult: BackendEnum<SrvaEventResult>,
    val eventResultDetail: BackendEnum<SrvaEventResultDetail>,
    val methods: List<CommonSrvaMethod>,
    val otherMethodDescription: String?,

    val personCount: Int,
    val hoursSpent: Int,
    val description: String?,
    val images: EntityImages,
)


internal fun CommonSrvaEvent.toSrvaEventData(): CommonSrvaEventData {
    return CommonSrvaEventData(
        localId = localId,
        localUrl = localUrl,
        remoteId = remoteId,
        revision = revision,
        mobileClientRefId = mobileClientRefId,
        srvaSpecVersion = srvaSpecVersion,
        state = state,
        rhyId = rhyId,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        location = location.asKnownLocation(),
        pointOfTime = pointOfTime,
        author = author,
        approver = approver,
        species = species,
        otherSpeciesDescription = otherSpeciesDescription,
        specimenAmount = totalSpecimenAmount,
        specimens = specimens.map { it.toCommonSpecimenData() },
        eventCategory = eventCategory,
        deportationOrderNumber = deportationOrderNumber,
        eventType = eventType,
        otherEventTypeDescription = otherEventTypeDescription,
        eventTypeDetail = eventTypeDetail,
        otherEventTypeDetailDescription = otherEventTypeDetailDescription,
        eventResult = eventResult,
        eventResultDetail = eventResultDetail,
        methods = methods,
        otherMethodDescription = otherMethodDescription,
        personCount = personCount,
        hoursSpent = hoursSpent,
        description = description,
        images = images,
    )
}
