package fi.riista.common.domain.srva.model

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class CommonSrvaEventData(
    val localId: Long?,
    val localUrl: String?, // Used as localId on iOS
    val remoteId: Long?,
    val revision: Long?,
    val mobileClientRefId: Long?,
    val srvaSpecVersion: Int,
    val state: BackendEnum<SrvaEventState>,
    val rhyId: Int?,

    val canEdit: Boolean,

    val location: CommonLocation,
    val pointOfTime: LocalDateTime,
    val author: CommonSrvaEventAuthor?,
    val approver: CommonSrvaEventApprover?,

    val species: Species,
    val otherSpeciesDescription: String?,
    // the amount of specimens as inputted by the user
    val specimenAmount: Int?,
    val specimens: List<CommonSpecimenData>,

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

    val personCount: Int?,
    val hoursSpent: Int?,
    val description: String?,
    val images: EntityImages,

    val modified: Boolean,
    val deleted: Boolean,
) {
    val selectedMethods: List<BackendEnum<SrvaMethodType>>
        get() = methods.selectedMethods

    fun approvedOrRejected(): Boolean {
        return state.value in listOf(SrvaEventState.APPROVED, SrvaEventState.REJECTED)
    }
}

internal fun CommonSrvaEventData.toSrvaEvent(): CommonSrvaEvent? {
    val knownLocation = (location as? CommonLocation.Known)?.etrsLocation
        ?: kotlin.run {
            return null
        }

    return CommonSrvaEvent(
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
        location = knownLocation,
        pointOfTime = pointOfTime,
        author = author,
        approver = approver,
        species = species,
        otherSpeciesDescription = otherSpeciesDescription,
        totalSpecimenAmount = specimenAmount ?: specimens.size,
        specimens = specimens.map { it.toSrvaSpecimen() },
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
        personCount = personCount ?: 0,
        hoursSpent = hoursSpent ?: 0,
        description = description,
        images = images,
    )
}

internal fun CommonSpecimenData.toSrvaSpecimen(): CommonSrvaSpecimen {
    return CommonSrvaSpecimen(
        gender = gender ?: BackendEnum.create(null),
        age = age ?: BackendEnum.create(null),
    )
}
