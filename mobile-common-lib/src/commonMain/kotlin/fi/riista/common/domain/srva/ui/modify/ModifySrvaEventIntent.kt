package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail

sealed class ModifySrvaEventIntent {
    class ChangeLocation(
        val location: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): ModifySrvaEventIntent()
    class ChangeSpecies(val species: Species): ModifySrvaEventIntent()
    class SetEntityImage(val image: EntityImage): ModifySrvaEventIntent()
    class ChangeSpecimenData(val specimenData: SpecimenFieldDataContainer): ModifySrvaEventIntent()
    class ChangeOtherSpeciesDescription(val description: String): ModifySrvaEventIntent()
    class ChangeDateAndTime(val dateAndTime: LocalDateTime): ModifySrvaEventIntent()
    class ChangeSpecimenAmount(val specimenAmount: Int?): ModifySrvaEventIntent()
    class ChangeEventCategory(val eventCategory: BackendEnum<SrvaEventCategoryType>): ModifySrvaEventIntent()
    class ChangeDeportationOrderNumber(val deportationOrderNumber: String): ModifySrvaEventIntent()
    class ChangeEventType(val eventType: BackendEnum<SrvaEventType>): ModifySrvaEventIntent()
    class ChangeOtherEventTypeDescription(val description: String): ModifySrvaEventIntent()
    class ChangeEventTypeDetail(val eventTypeDetail: BackendEnum<SrvaEventTypeDetail>): ModifySrvaEventIntent()
    class ChangeOtherEventTypeDetailDescription(val description: String): ModifySrvaEventIntent()
    class ChangeEventResult(val eventResult: BackendEnum<SrvaEventResult>): ModifySrvaEventIntent()
    class ChangeEventResultDetail(val eventResultDetail: BackendEnum<SrvaEventResultDetail>): ModifySrvaEventIntent()
    class ChangeMethodSelectionStatus(val methodIndex: Int, val selected: Boolean): ModifySrvaEventIntent()
    class ChangeOtherMethodDescription(val description: String): ModifySrvaEventIntent()
    class ChangePersonCount(val personCount: Int?): ModifySrvaEventIntent()
    class ChangeHoursSpent(val hoursSpent: Int?): ModifySrvaEventIntent()
    class ChangeDescription(val description: String): ModifySrvaEventIntent()
}
